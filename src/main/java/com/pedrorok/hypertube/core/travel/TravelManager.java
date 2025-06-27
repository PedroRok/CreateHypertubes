package com.pedrorok.hypertube.core.travel;

import com.pedrorok.hypertube.HypertubeMod;
import com.pedrorok.hypertube.blocks.HyperEntranceBlock;
import com.pedrorok.hypertube.config.ClientConfig;
import com.pedrorok.hypertube.core.sound.TubeSoundManager;
import com.pedrorok.hypertube.events.PlayerSyncEvents;
import com.pedrorok.hypertube.network.packets.PlayerTravelDirDataPacket;
import com.pedrorok.hypertube.network.packets.SyncPersistentDataPacket;
import com.pedrorok.hypertube.utils.MessageUtils;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
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

import static com.pedrorok.hypertube.core.travel.TravelConstants.*;

/**
 * @author Rok, Pedro Lucas nmm. Created on 22/04/2025
 * @project Create Hypertube
 */
public class TravelManager {

    private static final Map<UUID, TravelData> travelDataMap = new HashMap<>();

    public static void tryStartTravel(LivingEntity entity, BlockPos pos, BlockState state, float speed) {
        CompoundTag playerPersistData = entity.getPersistentData();
        if (playerPersistData.getBoolean(TRAVEL_TAG)) return;

        boolean isPlayer = entity instanceof ServerPlayer;
        ServerPlayer player = isPlayer ? (ServerPlayer) entity : null;

        if (isPlayer) {
            if (player.connection.latency() > LATENCY_THRESHOLD) {
                if (!entity.isShiftKeyDown()) {
                    MessageUtils.sendActionMessage(player, Component.translatable("hypertube.travel.latency")
                            .append(" (")
                            .append(Component.translatable("block.hypertube.hyper_entrance.sneak_to_enter"))
                            .append(")")
                            .withColor(0xff0000), true);
                    return;
                }
                MessageUtils.sendActionMessage(player, Component.empty(), true);
            }
        } else {
            speed = speed * 0.5f;
        }

        long lastTravelTime = playerPersistData.getLong(LAST_TRAVEL_TIME);

        if (playerPersistData.contains(LAST_TRAVEL_BLOCKPOS)) {
            BlockPos lastTravelPos = BlockPos.of(playerPersistData.getLong(LAST_TRAVEL_BLOCKPOS));
            if (lastTravelPos.equals(pos)
                && lastTravelTime > System.currentTimeMillis()) {
                return;
            }
        }

        if (lastTravelTime - DEFAULT_AFTER_TUBE_CAMERA > System.currentTimeMillis()) {
            speed += playerPersistData.getFloat(LAST_TRAVEL_SPEED);
        }

        BlockPos relative = pos.relative(state.getValue(HyperEntranceBlock.FACING));
        TravelData travelData = new TravelData(speed);
        travelData.init(relative, entity.level(), pos);

        if (travelData.getTravelPoints().size() < 3) {
            if (!isPlayer) return;
            MessageUtils.sendActionMessage(player, Component.translatable("hypertube.travel.too_short").withColor(0xff0000), true);
            return;
        }

        playerPersistData.putBoolean(TRAVEL_TAG, true);
        travelDataMap.put(entity.getUUID(), travelData);

        Vec3 center = pos.getCenter();
        entity.teleportTo(center.x, center.y, center.z);

        TubeSoundManager.playTubeSuctionSound(entity, center);

        syncPersistentData(entity);

        HypertubeMod.LOGGER.debug("Player start travel: {} to {} and speed {}", entity.getName().getString(), relative, travelData.getSpeed());
    }

    public static void entityTick(LivingEntity entity) {
        handleCommon(entity);
        if (entity.level().isClientSide && entity instanceof Player player) {
            clientTick(player);
            return;
        }
        handleServer(entity);
    }

    private static void handleCommon(LivingEntity entity) {
        if (hasHyperTubeData(entity)) {
            entity.refreshDimensions();
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

    public static void finishTravel(ServerPlayer player) {
        if (!travelDataMap.containsKey(player.getUUID())) return;
        finishTravel(player, travelDataMap.get(player.getUUID()), true);
    }

    private static void finishTravel(LivingEntity entity, TravelData travelData, boolean forced) {
        if (!(entity instanceof Player) && entity.level().isClientSide) return;
        travelDataMap.remove(entity.getUUID());
        entity.getPersistentData().putBoolean(TRAVEL_TAG, false);
        entity.getPersistentData().putLong(LAST_TRAVEL_TIME, System.currentTimeMillis() + DEFAULT_TRAVEL_TIME);
        entity.getPersistentData().putLong(LAST_TRAVEL_BLOCKPOS, travelData.getLastBlockPos().asLong());
        entity.getPersistentData().putFloat(LAST_TRAVEL_SPEED, travelData.getSpeed());
        entity.getPersistentData().putBoolean(IMMUNITY_TAG, true);

        Vec3 lastDir = travelData.getLastDir();
        Vec3 lastBlockPos = travelData.getLastBlockPos().getCenter();
        if (!forced) {
            if (entity.level() instanceof ServerLevel) {
                entity.teleportTo((ServerLevel) entity.level(), lastBlockPos.x, lastBlockPos.y, lastBlockPos.z, RelativeMovement.ALL, entity.getYRot(), entity.getXRot());
            }
            entity.teleportRelative(lastDir.x, lastDir.y, lastDir.z);
            entity.setDeltaMovement(lastDir.scale(travelData.getSpeed() + DEFAULT_MIN_SPEED));
        }
        entity.setPose(Pose.CROUCHING);
        entity.hurtMarked = true;

        syncPersistentData(entity);
        TubeSoundManager.playTubeSuctionSound(entity, entity.position());
    }

    private static void handleServer(LivingEntity entity) {
        if (!travelDataMap.containsKey(entity.getUUID())) {
            if (!entity.getPersistentData().getBoolean(TRAVEL_TAG)) return;
            entity.getPersistentData().putBoolean(TRAVEL_TAG, false);
            return;
        }
        handlePlayerTraveling(entity);
    }

    private static void handlePlayerTraveling(LivingEntity entity) {
        TravelData travelData = travelDataMap.get(entity.getUUID());
        Vec3 currentPoint = travelData.getTravelPoint();

        if (travelData.isFinished()) {
            finishTravel(entity, travelData, false);
            return;
        }

        if (entity.isSpectator() || entity.isDeadOrDying()) {
            finishTravel(entity, travelData, true);
            return;
        }

        entity.resetFallDistance();
        currentPoint = currentPoint.subtract(0, 0.25, 0);
        Vec3 entityPos = entity.position();
        double speed = 0.5D + travelData.getSpeed();

        Vec3 nextPoint = getNextPointPreview(travelData, 0);
        if (nextPoint == null) {
            Vec3 direction = currentPoint.subtract(entityPos).normalize();
            entity.setDeltaMovement(direction.scale(speed));
            entity.hurtMarked = true;
            return;
        }

        nextPoint = nextPoint.subtract(0, 0.25, 0);

        Vec3 segmentDirection = nextPoint.subtract(currentPoint).normalize();
        double segmentLength = currentPoint.distanceTo(nextPoint);

        handleEntityDirection(entity, segmentDirection);

        Vec3 toEntityPos = entityPos.subtract(currentPoint);
        double currentProjection = toEntityPos.dot(segmentDirection);
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
        Vec3 actualMovement = targetPosition.subtract(entityPos);

        double distanceFromLine = entityPos.distanceTo(currentIdealPosition);
        double correctionStrength = Math.min(1.0, distanceFromLine * 2.0);

        double distanceFromLineThreshold = entity instanceof Player ? DISTANCE_FROM_LINE_TP : DISTANCE_FROM_LINE_TP * 2;

        Vec3 correctedMovement = idealMovement.add(actualMovement.subtract(idealMovement).scale(correctionStrength));
        if (distanceFromLine > distanceFromLineThreshold) {
            //entity.teleportTo((ServerLevel) entity.level(), currentIdealPosition.x, currentIdealPosition.y, currentIdealPosition.z, RelativeMovement.ALL, entity.getYRot(), entity.getXRot());
            entity.moveTo(currentIdealPosition.x, currentIdealPosition.y, currentIdealPosition.z, entity.getYRot(), entity.getXRot());
        } else if (correctedMovement.length() > 0.5) {
            Vec3 movementDirection = correctedMovement.normalize();

            double smoothingFactor = Math.max(0.3, 0.5 - distanceFromLine);
            movementDirection = movementDirection.add(finalDirection.subtract(movementDirection).scale(smoothingFactor)).normalize();
            entity.setDeltaMovement(movementDirection.scale(speed));
        } else {
            entity.setDeltaMovement(finalDirection.scale(speed));
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
        checkAndCorrectStuck(entity, travelData);

        entity.hurtMarked = true;
    }

    private static void handleEntityDirection(LivingEntity entity, Vec3 direction) {
        float yaw = (float) Math.toDegrees(Math.atan2(-direction.x, direction.z));
        float pitch = (float) Math.toDegrees(Math.atan2(-direction.y, Math.sqrt(direction.x * direction.x + direction.z * direction.z)));

        entity.setYRot(yaw);
        entity.setXRot(pitch);
        if (entity instanceof Player player) {
            PacketDistributor.sendToPlayer((ServerPlayer) player, PlayerTravelDirDataPacket.create(entity));
        }
    }


    private static void checkAndCorrectStuck(LivingEntity entity, TravelData travelData) {
        if (!travelData.hasNextTravelPoint()) return;
        if (entity.tickCount % 5 != 0) return;

        float x = entity.getPersistentData().getFloat(LAST_POSITION + "_x");
        float y = entity.getPersistentData().getFloat(LAST_POSITION + "_y");
        float z = entity.getPersistentData().getFloat(LAST_POSITION + "_z");
        Vec3 lastPosition = new Vec3(x, y, z);


        if (entity.position().distanceTo(lastPosition) < 0.01) {
            // entity is stuck
            travelData.getNextTravelPoint();
            Vec3 travelPoint = travelData.getTravelPoint();
            entity.teleportTo(travelPoint.x, travelPoint.y, travelPoint.z);
            return;
        }
        entity.getPersistentData().putFloat(LAST_POSITION + "_x", (float) entity.position().x);
        entity.getPersistentData().putFloat(LAST_POSITION + "_y", (float) entity.position().y);
        entity.getPersistentData().putFloat(LAST_POSITION + "_z", (float) entity.position().z);
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


    private static void syncPersistentData(LivingEntity entity) {
        PlayerSyncEvents.syncPlayerStateToAll(entity, true);
        if (entity instanceof ServerPlayer player)
            PacketDistributor.sendToPlayer(player, SyncPersistentDataPacket.create(entity));
    }
}
