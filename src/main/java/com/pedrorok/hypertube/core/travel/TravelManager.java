package com.pedrorok.hypertube.core.travel;

import com.pedrorok.hypertube.HypertubeMod;
import com.pedrorok.hypertube.blocks.HyperEntranceBlock;
import com.pedrorok.hypertube.config.ClientConfig;
import com.pedrorok.hypertube.core.sound.TubeSoundManager;
import com.pedrorok.hypertube.events.PlayerSyncEvents;
import com.pedrorok.hypertube.network.NetworkHandler;
import com.pedrorok.hypertube.network.packets.MovePathPacket;
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
import java.util.Map;
import java.util.UUID;

import static com.pedrorok.hypertube.core.travel.TravelConstants.*;

/**
 * @author Rok, Pedro Lucas nmm. Created on 22/04/2025
 * @project Create Hypertube
 */
public class TravelManager {

    private static final Map<UUID, TravelPathMover> travelDataMap = new HashMap<>();

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
        TravelPathData travelPathData = new TravelPathData(relative, entity.level(), pos);

        if (travelPathData.getTravelPoints().size() < 3) {
            if (!isPlayer) return;
            MessageUtils.sendActionMessage(player, Component.translatable("hypertube.travel.too_short").withColor(0xff0000), true);
            return;
        }
        playerPersistData.putBoolean(TRAVEL_TAG, true);

        float finalSpeed = speed * 500;
        TravelPathMover pathMover = new TravelPathMover(
                entity.position(),
                travelPathData.getTravelPoints(),
                finalSpeed,
                travelPathData.getLastBlockPos(),
                TravelManager::finishTravel);
        travelDataMap.put(entity.getUUID(), pathMover);

        MovePathPacket movePathPacket = new MovePathPacket(entity.getId(), travelPathData.getTravelPoints(), finalSpeed);
        NetworkHandler.sendToTracking(movePathPacket, (ServerLevel) entity.level(), entity);
        Vec3 center = pos.getCenter();
        TubeSoundManager.playTubeSuctionSound(entity, center);

        syncPersistentData(entity);

        HypertubeMod.LOGGER.debug("Player start travel: {} to {} and speed {}", entity.getName().getString(), relative, pathMover.getTravelSpeed());
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
        finishTravel(player, true);
    }

    private static void finishTravel(LivingEntity entity, boolean forced) {
        if (!(entity instanceof Player) && entity.level().isClientSide) return;
        TravelPathMover pathMover = travelDataMap.get(entity.getUUID());
        travelDataMap.remove(entity.getUUID());
        entity.getPersistentData().putBoolean(TRAVEL_TAG, false);
        entity.getPersistentData().putLong(LAST_TRAVEL_TIME, System.currentTimeMillis() + DEFAULT_TRAVEL_TIME);
        entity.getPersistentData().putLong(LAST_TRAVEL_BLOCKPOS, pathMover.getLastBlockPos().asLong());
        entity.getPersistentData().putFloat(LAST_TRAVEL_SPEED, pathMover.getTravelSpeed());
        entity.getPersistentData().putBoolean(IMMUNITY_TAG, true);

        syncPersistentData(entity);

        Vec3 lastDir = pathMover.getLastDir();
        Vec3 lastBlockPos = pathMover.getLastBlockPos().getCenter();
        if (!forced) {
            if (entity.level() instanceof ServerLevel) {
                entity.teleportTo((ServerLevel) entity.level(), lastBlockPos.x, lastBlockPos.y, lastBlockPos.z, RelativeMovement.ALL, entity.getYRot(), entity.getXRot());
            }
            entity.setDeltaMovement(lastDir.scale(pathMover.getTravelSpeed() + DEFAULT_MIN_SPEED));
        }
        entity.hurtMarked = true;

        entity.setPose(Pose.SWIMMING);

        TubeSoundManager.playTubeSuctionSound(entity, entity.position());
    }

    private static void handleServer(LivingEntity entity) {
        if (!travelDataMap.containsKey(entity.getUUID())) {
            if (!entity.getPersistentData().getBoolean(TRAVEL_TAG)) return;
            entity.getPersistentData().putBoolean(TRAVEL_TAG, false);
            return;
        }
        TravelPathMover travelPathData = travelDataMap.get(entity.getUUID());
        entity.resetFallDistance();
        travelPathData.tickPlayer(entity);
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
