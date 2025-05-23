package com.pedrorok.hypertube.managers;

import com.pedrorok.hypertube.HypertubeMod;
import com.pedrorok.hypertube.blocks.HyperEntranceBlock;
import com.pedrorok.hypertube.camera.DetachedCameraController;
import com.pedrorok.hypertube.utils.MathUtils;
import com.simibubi.create.foundation.networking.ISyncPersistentData;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
        playerPersistData.putBoolean(TRAVEL_TAG, true);

        PacketDistributor.sendToPlayer(player, new ISyncPersistentData.PersistentDataPacket(player));
        BlockPos relative = pos.relative(state.getValue(HyperEntranceBlock.FACING));
        TravelData travelData = new TravelData(relative, player.level(), pos, MathUtils.getMediumSpeed(player.getDeltaMovement()));
        HypertubeMod.LOGGER.debug("Player start travel: {} to {} and speed {}", player.getName().getString(), relative, travelData.getSpeed());
        travelDataMap.put(player.getUUID(), travelData);
    }


    public static void removePlayerFromTravel(Player player) {
        travelDataMap.remove(player.getUUID());
        player.getPersistentData().putBoolean(TRAVEL_TAG, false);
        player.getPersistentData().putLong(LAST_TRAVEL_TIME, System.currentTimeMillis() + DEFAULT_TRAVEL_TIME);
        PacketDistributor.sendToPlayer((ServerPlayer) player, new ISyncPersistentData.PersistentDataPacket(player));
        player.refreshDimensions();
    }

    public static void playerTick(Player player) {
        handleCommon(player);
        if (player.level().isClientSide) {
            handleClient(player);
            return;
        }
        handleServer(player);
    }

    private static void handleCommon(Player player) {
        if (hasHyperTubeData(player)) {
            player.refreshDimensions();
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleClient(Player player) {
        Minecraft mc = Minecraft.getInstance();
        if ((!hasHyperTubeData(player)
             || mc.options.getCameraType().isFirstPerson())) {
            DetachedCameraController.get().setDetached(false);
            return;
        }
        if (!DetachedCameraController.get().isDetached()) {
            DetachedCameraController.get().startCamera(player);
            DetachedCameraController.get().setDetached(true);
        }
    }

    private static void handleServer(Player player) {
        if (!travelDataMap.containsKey(player.getUUID())) return;
        TravelData travelData = travelDataMap.get(player.getUUID());
        Vec3 point = travelData.getTravelPoint();
        if (point == null) {
            travelDataMap.remove(player.getUUID());
            player.getPersistentData().putBoolean(TRAVEL_TAG, false);
            // --- NOTE: this is just to make easy to debug
            player.getPersistentData().putLong(LAST_TRAVEL_TIME, System.currentTimeMillis() + DEFAULT_TRAVEL_TIME);
            // ---
            PacketDistributor.sendToPlayer((ServerPlayer) player, new ISyncPersistentData.PersistentDataPacket(player));

            // TODO: Persist velocity
            Vec3 lastDir = travelData.getLastDir().scale(3);
            player.teleportRelative(lastDir.x, lastDir.y, lastDir.z);
            player.setDeltaMovement(travelData.getLastDir().scale(travelData.getSpeed() + 0.5));
            player.setPose(Pose.CROUCHING);
            player.hurtMarked = true;
            return;
        }
        point = point.subtract(0, 0.25, 0);
        double distance = player.distanceToSqr(point.x, point.y, point.z);
        if (distance > 0.6D) {
            Vec3 travelNormal = point.subtract(player.position()).normalize();
            player.setDeltaMovement(travelNormal.scale(0.5D + travelData.getSpeed()));
            player.hurtMarked = true;
        } else {
            travelData.getNextTravelPoint();
            if (travelData.getTravelPoint() == null) return;
            travelData.getNextTravelPoint();
            if (travelData.getTravelPoint() == null) return;
            travelData.setLastDir(travelData.getTravelPoint().subtract(point).normalize());
        }
    }

    public static boolean hasHyperTubeData(Entity player) {
        return player.getPersistentData().getBoolean(TRAVEL_TAG);
    }

}
