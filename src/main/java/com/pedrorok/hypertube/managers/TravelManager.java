package com.pedrorok.hypertube.managers;

import com.pedrorok.hypertube.HypertubeMod;
import com.pedrorok.hypertube.blocks.HyperEntranceBlock;
import com.pedrorok.hypertube.config.ClientConfig;
import com.pedrorok.hypertube.events.PlayerSyncEvents;
import com.pedrorok.hypertube.managers.sound.TubeSoundManager;
import com.pedrorok.hypertube.registry.ModSounds;
import com.simibubi.create.foundation.networking.ISyncPersistentData;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author Rok, Pedro Lucas nmm. Created on 22/04/2025
 * @project Create Hypertube
 */
public class TravelManager {

    public static final String TRAVEL_TAG = "hypertube_travel";
    public static final String LAST_TRAVEL_TIME = "last_travel_time";

    public static final String LAST_TRAVEL_BLOCKPOS = "last_travel_blockpos";
    public static final String LAST_TRAVEL_SPEED = "last_travel_speed";
    public static final String LAST_POSITION = "last_travel_position";

    public static final String IMMUNITY_TAG = "hypertube_immunity";

    public static final int DEFAULT_TRAVEL_TIME = 2000;
    public static final int DEFAULT_AFTER_TUBE_CAMERA = 1500; // 0.5 seconds (subtracting default travel time)

    private static final Map<UUID, TravelData> travelDataMap = new HashMap<>();

    public static void tryStartTravel(ServerPlayer player, BlockPos pos, BlockState state, float speed) {
        CompoundTag playerPersistData = player.getPersistentData();
        if (playerPersistData.getBoolean(TRAVEL_TAG)) return;
        long lastTravelTime = playerPersistData.getLong(LAST_TRAVEL_TIME);

        if (playerPersistData.contains(LAST_TRAVEL_BLOCKPOS)) {
            BlockPos lastTravelPos = BlockPos.of(playerPersistData.getLong(LAST_TRAVEL_BLOCKPOS));
            if (lastTravelPos.equals(pos)
                && lastTravelTime > System.currentTimeMillis()) {
                return;
            }
        }

        if (lastTravelTime - TravelManager.DEFAULT_AFTER_TUBE_CAMERA > System.currentTimeMillis()) {
            speed += playerPersistData.getFloat(LAST_TRAVEL_SPEED); // Increase speed if player is trying to fast travel
        }

        BlockPos relative = pos.relative(state.getValue(HyperEntranceBlock.FACING));
        TravelData travelData = new TravelData(relative, player.level(), pos, speed);

        if (travelData.getTravelPoints().size() < 3) {
            // TODO: Handle error
            return;
        }

        playerPersistData.putBoolean(TRAVEL_TAG, true);

        travelDataMap.put(player.getUUID(), travelData);
        PlayerSyncEvents.syncPlayerStateToAll(player);

        Vec3 center = pos.getCenter();

        Vec3 eyePos = player.getEyePosition();
        Vec3 playerPos = player.position();
        if (playerPos.distanceTo(center) > eyePos.distanceTo(center)) {
            player.teleportRelative(0, 1, 0);
        }

        playHypertubeSuctionSound(player, center);
        PacketDistributor.sendToPlayer(player, new ISyncPersistentData.PersistentDataPacket(player));
        HypertubeMod.LOGGER.debug("Player start travel: {} to {} and speed {}", player.getName().getString(), relative, travelData.getSpeed());
    }

    public static void playerTick(Player player) {
        handleCommon(player);
        if (player.level().isClientSide) {
            clientTick(player);
            return;
        }
        handleServer(player);
    }

    private static void handleCommon(Player player) {
        if (hasHyperTubeData(player)) {
            player.refreshDimensions();
        }
    }

    private static boolean isTraveling;

    @OnlyIn(Dist.CLIENT)
    private static void clientTick(Player player) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        if (!mc.player.is(player)) return;

        if (hasHyperTubeData(player)) {
            TubeSoundManager.TravelSound.enableClientPlayerSound(player, 0.8F, 1.0F);
            isTraveling = true;
            return;
        }
        if (isTraveling
            && !ClientConfig.get().ALLOW_FPV_INSIDE_TUBE.get()) {
            Minecraft.getInstance().options.setCameraType(CameraType.FIRST_PERSON);
            isTraveling = false;
        }
    }

    private static void finishTravel(ServerPlayer player, TravelData travelData, boolean forced) {

        PlayerSyncEvents.syncPlayerStateToAll(player);

        travelDataMap.remove(player.getUUID());
        player.getPersistentData().putBoolean(TRAVEL_TAG, false);
        // --- NOTE: this is just to make easy to debug
        player.getPersistentData().putLong(LAST_TRAVEL_TIME, System.currentTimeMillis() + DEFAULT_TRAVEL_TIME);
        player.getPersistentData().putLong(LAST_TRAVEL_BLOCKPOS, travelData.getLastBlockPos().asLong());
        player.getPersistentData().putFloat(LAST_TRAVEL_SPEED, travelData.getSpeed());
        player.getPersistentData().putBoolean(IMMUNITY_TAG, true);
        // ---
        PacketDistributor.sendToPlayer(player, new ISyncPersistentData.PersistentDataPacket(player));

        Vec3 lastDir = travelData.getLastDir();
        Vec3 lastBlockPos = travelData.getLastBlockPos().getCenter();
        if (!forced) {
            player.teleportTo((ServerLevel) player.level(), lastBlockPos.x, lastBlockPos.y, lastBlockPos.z, player.getYRot(), player.getXRot());
            player.teleportRelative(lastDir.x, lastDir.y, lastDir.z);
            player.setDeltaMovement(travelData.getLastDir().scale(travelData.getSpeed() + 0.5));
        }
        player.setPose(Pose.CROUCHING);
        player.hurtMarked = true;

        PlayerSyncEvents.syncPlayerStateToAll(player);

        playHypertubeSuctionSound(player, player.position());
    }

    private static void handleServer(Player player) {
        if (!travelDataMap.containsKey(player.getUUID())) {
            if (!player.getPersistentData().getBoolean(TRAVEL_TAG)) return;
            player.getPersistentData().putBoolean(TRAVEL_TAG, false);
            return;
        }
        handlePlayerTraveling(player);
    }

    private static void handlePlayerTraveling(Player player) {
        TravelData travelData = travelDataMap.get(player.getUUID());
        Vec3 currentPoint = travelData.getTravelPoint();

        if (travelData.isFinished()) {
            finishTravel((ServerPlayer) player, travelData, false);
            return;
        }

        if (player.isSpectator() || player.isDeadOrDying()) {
            finishTravel((ServerPlayer) player, travelData, true);
            return;
        }

        player.resetFallDistance();
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

        if (correctedMovement.length() > 0.5) {
            Vec3 movementDirection = correctedMovement.normalize();

            double smoothingFactor = Math.max(0.3, 0.5 - distanceFromLine);
            movementDirection = movementDirection.add(finalDirection.subtract(movementDirection).scale(smoothingFactor)).normalize();
            if (distanceFromLine > 1.5) {
                player.teleportTo((ServerLevel) player.level(), currentIdealPosition.x, currentIdealPosition.y, currentIdealPosition.z, RelativeMovement.ROTATION,  player.getYRot(), player.getXRot());
            } else {
                player.setDeltaMovement(movementDirection.scale(speed));
            }
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
        checkAndCorrectStuck(player, travelData);

        player.hurtMarked = true;
    }


    private static void checkAndCorrectStuck(Player player, TravelData travelData) {
        if (!travelData.hasNextTravelPoint()) return;
        if (player.tickCount % 5 != 0) return;

        float x = player.getPersistentData().getFloat(LAST_POSITION + "_x");
        float y = player.getPersistentData().getFloat(LAST_POSITION + "_y");
        float z = player.getPersistentData().getFloat(LAST_POSITION + "_z");
        Vec3 lastPosition = new Vec3(x, y, z);


        if (player.position().distanceTo(lastPosition) < 0.01) {
            // player is stuck
            travelData.getNextTravelPoint();
            Vec3 travelPoint = travelData.getTravelPoint();
            player.teleportTo(travelPoint.x, travelPoint.y, travelPoint.z);
            return;
        }
        player.getPersistentData().putFloat(LAST_POSITION + "_x", (float) player.position().x);
        player.getPersistentData().putFloat(LAST_POSITION + "_y", (float) player.position().y);
        player.getPersistentData().putFloat(LAST_POSITION + "_z", (float) player.position().z);
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

    private static void playHypertubeSuctionSound(ServerPlayer player, Vec3 pos) {
        RandomSource random = player.level().random;
        float pitch = 0.8F + random.nextFloat() * 0.4F;
        int seed = random.nextInt(1000);
        for (Player oPlayer : player.level().players()) {
            ((ServerPlayer) oPlayer).connection.send(new ClientboundSoundPacket(ModSounds.HYPERTUBE_SUCTION,
                    SoundSource.BLOCKS, pos.x, pos.y, pos.z, 1, pitch, seed));
        }
    }

    public static boolean hasHyperTubeData(Entity player) {
        return player.getPersistentData().getBoolean(TRAVEL_TAG);
    }
}
