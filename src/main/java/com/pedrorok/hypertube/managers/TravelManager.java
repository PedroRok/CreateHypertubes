package com.pedrorok.hypertube.managers;

import com.pedrorok.hypertube.HypertubeMod;
import com.pedrorok.hypertube.blocks.HyperEntranceBlock;
import com.pedrorok.hypertube.events.PlayerSyncEvents;
import com.pedrorok.hypertube.registry.ModSounds;
import com.pedrorok.hypertube.utils.MathUtils;
import com.simibubi.create.foundation.networking.ISyncPersistentData;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;

/**
 * @author Rok, Pedro Lucas nmm. Created on 22/04/2025
 * @project Create Hypertube
 */
public class TravelManager {

    public static final String TRAVEL_TAG = "hypertube_travel";
    public static final String LAST_TRAVEL_TIME = "last_travel_time";

    public static final int DEFAULT_TRAVEL_TIME = 200;

    private static final Map<UUID, TravelData> travelDataMap = new HashMap<>();

    public static void tryStartTravel(ServerPlayer player, BlockPos pos, BlockState state) {
        CompoundTag playerPersistData = player.getPersistentData();
        if (playerPersistData.getBoolean(TRAVEL_TAG)) return;
        if (playerPersistData.contains(LAST_TRAVEL_TIME) &&
            playerPersistData.getLong(LAST_TRAVEL_TIME) > System.currentTimeMillis()) return;
        //player.setNoGravity(true);

        BlockPos relative = pos.relative(state.getValue(HyperEntranceBlock.FACING));
        TravelData travelData = new TravelData(relative, player.level(), pos, MathUtils.getMediumSpeed(player.getDeltaMovement()));

        if (travelData.getTravelPoints().size() < 3) {
            // TODO: Handle error
            return;
        }

        playerPersistData.putBoolean(TRAVEL_TAG, true);
        PacketDistributor.sendToPlayer(player, new ISyncPersistentData.PersistentDataPacket(player));

        HypertubeMod.LOGGER.debug("Player start travel: {} to {} and speed {}", player.getName().getString(), relative, travelData.getSpeed());
        travelDataMap.put(player.getUUID(), travelData);
        PlayerSyncEvents.syncPlayerStateToAll(player);

        RandomSource random = player.level().random;

        float pitch = 0.8F + random.nextFloat() * 0.4F;
        int seed = random.nextInt(1000);
        for (Player oPlayer : player.level().players()) {
            ((ServerPlayer) oPlayer).connection.send(new ClientboundSoundPacket(ModSounds.HYPERTUBE_SUCTION,
                    SoundSource.BLOCKS, pos.getX(), pos.getY(), pos.getZ(), 1, pitch, seed));
        }
        player.connection.send(new ClientboundSoundEntityPacket(ModSounds.TRAVELING,
                SoundSource.BLOCKS, player, 1, 1, 1));
    }


    public static void removePlayerFromTravel(Player player) {
        travelDataMap.remove(player.getUUID());
        player.getPersistentData().putBoolean(TRAVEL_TAG, false);
        player.getPersistentData().putLong(LAST_TRAVEL_TIME, System.currentTimeMillis() + DEFAULT_TRAVEL_TIME);
        PacketDistributor.sendToPlayer((ServerPlayer) player, new ISyncPersistentData.PersistentDataPacket(player));
        player.refreshDimensions();
        PlayerSyncEvents.syncPlayerStateToAll((ServerPlayer) player);
    }

    public static void playerTick(Player player) {
        handleCommon(player);
        if (player.level().isClientSide) {
            return;
        }
        handleServer(player);
    }

    private static void handleCommon(Player player) {
        if (hasHyperTubeData(player)) {
            player.refreshDimensions();
        }
    }

    private static void finishTravel(ServerPlayer player, TravelData travelData) {

        PlayerSyncEvents.syncPlayerStateToAll(player);

        travelDataMap.remove(player.getUUID());
        player.getPersistentData().putBoolean(TRAVEL_TAG, false);
        // --- NOTE: this is just to make easy to debug
        player.getPersistentData().putLong(LAST_TRAVEL_TIME, System.currentTimeMillis() + DEFAULT_TRAVEL_TIME);
        // ---
        PacketDistributor.sendToPlayer(player, new ISyncPersistentData.PersistentDataPacket(player));

        // TODO: Persist velocity
        Vec3 lastDir = travelData.getLastDir().scale(3);
        player.teleportRelative(lastDir.x, lastDir.y, lastDir.z);
        player.setDeltaMovement(travelData.getLastDir().scale(travelData.getSpeed() + 0.5));
        player.setPose(Pose.CROUCHING);
        player.hurtMarked = true;

        PlayerSyncEvents.syncPlayerStateToAll(player);

        RandomSource random = player.level().random;
        float pitch = 0.8F + random.nextFloat() * 0.4F;
        int seed = random.nextInt(1000);
        player.connection.send(new ClientboundStopSoundPacket(ModSounds.TRAVELING.getId(),
                SoundSource.BLOCKS));
        for (Player oPlayer : player.level().players()) {
            ((ServerPlayer) oPlayer).connection.send(new ClientboundSoundPacket(ModSounds.HYPERTUBE_SUCTION,
                    SoundSource.BLOCKS, player.getX(), player.getY(), player.getZ(), 1, pitch, seed));
        }
    }

    private static void handleServer(Player player) {
        if (!travelDataMap.containsKey(player.getUUID())) return;
        TravelData travelData = travelDataMap.get(player.getUUID());
        Vec3 currentPoint = travelData.getTravelPoint();

        if (travelData.isFinished()) {
            finishTravel((ServerPlayer) player, travelData);
            return;
        }

        currentPoint = currentPoint.subtract(0, 0.25, 0);
        Vec3 playerPos = player.position();
        double speed = 0.5D + travelData.getSpeed();

        Vec3 nextPoint = getNextPointPreview(travelData, 0);
        if (nextPoint == null) {
            Vec3 direction = currentPoint.subtract(playerPos).normalize();
            player.setDeltaMovement(direction.scale(speed));
            player.hurtMarked = true;
            return;
        }

        nextPoint = nextPoint.subtract(0, 0.25, 0);

        Vec3 segmentDirection = nextPoint.subtract(currentPoint).normalize();
        double segmentLength = currentPoint.distanceTo(nextPoint);

        Vec3 toPlayer = playerPos.subtract(currentPoint);
        double currentProjection = toPlayer.dot(segmentDirection);
        currentProjection = Math.max(0, Math.min(segmentLength, currentProjection));

        Vec3 currentIdealPosition = currentPoint.add(segmentDirection.scale(currentProjection));

        double nextProjection = currentProjection + speed;

        Vec3 targetPosition;
        Vec3 finalDirection;
        boolean shouldAdvanceWaypoint = false;

        if (nextProjection >= segmentLength * 0.95) {
            shouldAdvanceWaypoint = true;

            Vec3 nextNextPoint = getNextPointPreview(travelData, 1);
            if (nextNextPoint != null) {
                nextNextPoint = nextNextPoint.subtract(0, 0.25, 0);

                double overflow = nextProjection - segmentLength;

                Vec3 nextSegmentDirection = nextNextPoint.subtract(nextPoint).normalize();

                targetPosition = nextPoint.add(nextSegmentDirection.scale(overflow));

                double transitionFactor = Math.min(1.0, (nextProjection - segmentLength * 0.8) / (segmentLength * 0.2));
                finalDirection = segmentDirection.add(nextSegmentDirection.subtract(segmentDirection).scale(transitionFactor)).normalize();
            } else {
                targetPosition = nextPoint;
                finalDirection = segmentDirection;
            }
        } else {
            targetPosition = currentPoint.add(segmentDirection.scale(nextProjection));
            finalDirection = segmentDirection;
        }

        Vec3 idealMovement = targetPosition.subtract(currentIdealPosition);
        Vec3 actualMovement = targetPosition.subtract(playerPos);

        double distanceFromLine = playerPos.distanceTo(currentIdealPosition);
        double correctionStrength = Math.min(1.0, distanceFromLine * 2.0);

        Vec3 correctedMovement = idealMovement.add(actualMovement.subtract(idealMovement).scale(correctionStrength));

        if (correctedMovement.length() > 0.001) {
            Vec3 movementDirection = correctedMovement.normalize();

            double smoothingFactor = Math.max(0.3, 0.5 - distanceFromLine);
            movementDirection = movementDirection.add(finalDirection.subtract(movementDirection).scale(smoothingFactor)).normalize();

            player.setDeltaMovement(movementDirection.scale(speed));
        } else {
            player.setDeltaMovement(finalDirection.scale(speed));
        }

        if (shouldAdvanceWaypoint) {
            travelData.getNextTravelPoint();
            if (travelData.getTravelPoint() != null) {
                Vec3 newNextPoint = getNextPointPreview(travelData, 0);
                if (newNextPoint != null) {
                    Vec3 newDirection = newNextPoint.subtract(travelData.getTravelPoint()).normalize();
                    travelData.setLastDir(newDirection);
                }
            }
        }

        player.hurtMarked = true;
    }

    private static Vec3 getNextPointPreview(TravelData travelData, int offset) {
        List<Vec3> points = travelData.getTravelPoints();
        int currentIndex = travelData.getTravelIndex();
        int targetIndex = currentIndex + 1 + offset;

        if (targetIndex < points.size()) {
            return points.get(targetIndex);
        }
        travelData.setFinished(true);
        return null;
    }

    public static boolean hasHyperTubeData(Entity player) {
        return player.getPersistentData().getBoolean(TRAVEL_TAG);
    }

}
