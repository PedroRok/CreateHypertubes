package com.pedrorok.hypertube.events;

import com.pedrorok.hypertube.HypertubeMod;
import com.pedrorok.hypertube.blocks.HyperEntranceBlock;
import com.pedrorok.hypertube.blocks.HypertubeBlock;
import com.simibubi.create.foundation.networking.ISyncPersistentData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;

/**
 * @author Rok, Pedro Lucas nmm. Created on 22/04/2025
 * @project Create Hypertube
 */
@EventBusSubscriber(modid = HypertubeMod.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class TravelManager {

    private static final Map<UUID, TravelData> travelDataMap = new HashMap<>();

    public static void tryStartTravel(ServerPlayer player, BlockPos pos, BlockState state) {
        if (player.getPersistentData().getBoolean(HypertubeBlock.TRAVEL_TAG)) return;
        player.getPersistentData().putBoolean(HypertubeBlock.TRAVEL_TAG, true);

        PacketDistributor.sendToPlayer(player, new ISyncPersistentData.PersistentDataPacket(player));
        BlockPos relative = pos.relative(state.getValue(HyperEntranceBlock.FACING));
        TravelData travelData = new TravelData(relative, player.level(), pos);

        travelDataMap.put(player.getUUID(), travelData);
    }

    @SubscribeEvent
    public static void playerTick(PlayerTickEvent.Post tickEvent) {
        Player entity = tickEvent.getEntity();
        if (entity.level().isClientSide) {
            handleClient(entity);
            return;
        }
        handleServer(entity);
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleClient(Player player) {
        if (player.getPersistentData().getBoolean(HypertubeBlock.TRAVEL_TAG)) {
            player.setPose(Pose.SWIMMING);
        }
    }

    private static void handleServer(Player player) {
        if (!travelDataMap.containsKey(player.getUUID())) return;
        TravelData travelData = travelDataMap.get(player.getUUID());
        BlockPos travelPoint = travelData.getTravelPoint();
        if (travelPoint == null) {
            travelDataMap.remove(player.getUUID());
            player.getPersistentData().putBoolean(HypertubeBlock.TRAVEL_TAG, false);
            PacketDistributor.sendToPlayer((ServerPlayer) player, new ISyncPersistentData.PersistentDataPacket(player));
            return;
        }
        Vec3 point = travelPoint.getCenter();
        double distance = player.distanceToSqr(point.x, point.y, point.z);
        if (distance > 0.4D) {
            Vec3 travelNormal = point.subtract(player.position()).normalize();
            player.setDeltaMovement(travelNormal.scale(0.5D));
            player.hurtMarked = true;
        } else {
            travelData.getNextTravelPoint();
        }
    }


    private static class TravelData {

        private final List<BlockPos> travelPoints;
        private int travelIndex;

        public TravelData(BlockPos firstPipe, Level level, BlockPos entrancePos) {
            this.travelPoints = new ArrayList<>();
            travelPoints.add(entrancePos);
            travelPoints.add(firstPipe);

            addTravelPoint(firstPipe, level);
        }

        private void addTravelPoint(BlockPos pos, Level level) {
            BlockState blockState = level.getBlockState(pos);
            HypertubeBlock pipeBlock = (HypertubeBlock) blockState.getBlock();
            List<Direction> connectedFaces = pipeBlock.getConnectedFaces(blockState);
            for (Direction direction : connectedFaces) {
                BlockPos nextPipe = pos.relative(direction);
                if (travelPoints.contains(nextPipe)) continue;
                travelPoints.add(nextPipe);
                addTravelPoint(nextPipe, level);
                break;
            }
        }

        public BlockPos getTravelPoint() {
            if (travelIndex >= travelPoints.size()) return null;
            return travelPoints.get(travelIndex);
        }

        public void getNextTravelPoint() {
            if (travelIndex >= travelPoints.size()) return;
            BlockPos nextTravelPoint = travelPoints.get(travelIndex);
            travelIndex++;
        }
    }
}
