package com.pedrorok.hypertube.core.travel;

import com.mojang.datafixers.util.Pair;
import com.pedrorok.hypertube.HypertubeMod;
import com.pedrorok.hypertube.blocks.HyperEntranceBlock;
import com.pedrorok.hypertube.blocks.HyperJunctionBlock;
import com.pedrorok.hypertube.config.ClientConfig;
import com.pedrorok.hypertube.core.compat.Mods;
import com.pedrorok.hypertube.core.compat.sable.SableCompat;
import com.pedrorok.hypertube.core.sound.TubeSoundManager;
import com.pedrorok.hypertube.events.PlayerSyncEvents;
import com.pedrorok.hypertube.network.packets.MovePathPacket;
import com.pedrorok.hypertube.network.packets.SyncPersistentDataPacket;
import com.pedrorok.hypertube.utils.JunctionDirectionUtils;
import com.pedrorok.hypertube.utils.MessageUtils;
import com.pedrorok.hypertube.utils.MoveDirection;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.UUID;

import static com.pedrorok.hypertube.core.travel.TravelConstants.*;

/**
 * @author Rok, Pedro Lucas nmm. Created on 22/04/2025
 * @project Create Hypertube
 */
public class TravelManager {

    private static final Object2ObjectArrayMap<UUID, TravelPathMover> travelDataMap = new Object2ObjectArrayMap<>();

    public static boolean tryStartTravel(LivingEntity entity, BlockEntity blockEntity, Direction facingDirection, float speed) {
        BlockState state = blockEntity.getBlockState();
        BlockPos pos = blockEntity.getBlockPos();
        boolean isJunction = state.getBlock() instanceof HyperJunctionBlock;

        CompoundTag entityPersistentData = entity.getPersistentData();
        if (entityPersistentData.getBoolean(TRAVEL_TAG) && !isJunction) return false;

        boolean isPlayer = entity instanceof ServerPlayer;
        ServerPlayer player = isPlayer ? (ServerPlayer) entity : null;

        long lastTravelTime = entityPersistentData.getLong(LAST_TRAVEL_TIME);

        if (entityPersistentData.contains(LAST_TRAVEL_BLOCKPOS) && !isJunction) {
            BlockPos lastTravelPos = BlockPos.of(entityPersistentData.getLong(LAST_TRAVEL_BLOCKPOS));
            if (lastTravelPos.equals(pos)
                    && lastTravelTime > System.currentTimeMillis()) {
                return false;
            }
        }

        if (lastTravelTime - DEFAULT_AFTER_TUBE_CAMERA > System.currentTimeMillis() && !isJunction) {
            speed += entityPersistentData.getFloat(LAST_TRAVEL_SPEED);
        }

        TravelPathData travelPathData = new TravelPathData(facingDirection, entity.level(), pos);

        if (travelPathData.getTravelPoints().size() < 3) {
            if (!isPlayer) return false;
            MessageUtils.sendActionMessage(player, Component.translatable("hypertube.travel.too_short").withColor(0xff0000), true);
            return false;
        }
        entityPersistentData.putBoolean(TRAVEL_TAG, true);

        float finalSpeed = (speed * TravelConstants.DEFAULT_SPEED_MULTIPLIER);

        TravelPathMover pathMover = new TravelPathMover(
                blockEntity,
                travelPathData,
                entity,
                finalSpeed,
                TravelManager::finishTravel);
        travelDataMap.put(entity.getUUID(), pathMover);

        MovePathPacket movePathPacket = new MovePathPacket(entity.getId(),
                travelPathData.getTravelPoints(),
                travelPathData.getActionPoints(),
                finalSpeed,
                travelPathData.isFinishWithJunction(),
                travelPathData.getJunctionDirection());
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, movePathPacket);
        Vec3 center = pos.getCenter();
        Mods.SABLE.executeIfInstalled(() -> () -> SableCompat.stickToSubLevel(entity, center));

        syncPersistentData(entity);

        HypertubeMod.LOGGER.debug("Travel started: {} to {} and speed {}", entity.getName().getString(), pos, pathMover.getTravelSpeed());
        return true;
    }

    public static void entityTick(LivingEntity entity) {
        handleCommon(entity);
        if (entity.level().isClientSide && entity instanceof Player player) {
            clientTick(player);
            return;
        }
        if (entity.level().isClientSide) return;
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
        finishTravel(EndTravelData.forced(player));
    }

    private static void finishTravel(EndTravelData data) {
        final boolean forced = data.isForced();
        final LivingEntity entity = data.entity();
        final Level level = entity.level();
        if (level.isClientSide) return;
        TravelPathMover pathMover = travelDataMap.get(entity.getUUID());
        travelDataMap.remove(entity.getUUID());
        entity.getPersistentData().putBoolean(TRAVEL_TAG, false);
        entity.getPersistentData().putLong(LAST_TRAVEL_TIME, System.currentTimeMillis() + DEFAULT_TRAVEL_TIME);
        entity.getPersistentData().putLong(LAST_TRAVEL_BLOCKPOS, pathMover.getLastPos().asLong());
        float finalSpeed = pathMover.getTravelSpeed();
        entity.getPersistentData().putFloat(LAST_TRAVEL_SPEED, finalSpeed);
        entity.getPersistentData().putBoolean(IMMUNITY_TAG, true);

        syncPersistentData(entity);

        if (data.isJunctionEnd()) {
            BlockEntity blockState = level.getBlockEntity(pathMover.getLastPos());
            if (!tryStartTravel(entity, blockState, data.direction(), pathMover.getTravelSpeed())) return;
            TubeSoundManager.playTubeSuctionSound(entity, pathMover.getLastPos().getCenter(), 0.5f, 1.2f);
            return;
        }

        Vec3 lastDir = pathMover.getLastDir();
        Vec3 lastBlockPos = pathMover.getLastPos().getCenter();
        BlockState blockState = level.getBlockState(BlockPos.containing(lastBlockPos));
        if (blockState.getBlock() instanceof HyperEntranceBlock) {
            lastBlockPos = pathMover.getLastPos().relative(blockState.getValue(HyperEntranceBlock.FACING).getOpposite()).getCenter();
        }

        Pair<Vec3, Vec3> lastPosDir = Pair.of(lastBlockPos, lastDir);
        lastPosDir = Mods.SABLE.executeIfInstalled(() -> (posDir) -> SableCompat.transformToWorld(level, posDir.getFirst(), posDir.getSecond()), lastPosDir);
        lastBlockPos = lastPosDir.getFirst();
        lastDir = lastPosDir.getSecond();
        lastBlockPos = lastBlockPos.add(lastDir.scale(0.5));

        Mods.SABLE.executeIfInstalled(() -> () -> SableCompat.stickToSubLevel(entity, null));
        if (!forced) {
            if (level instanceof ServerLevel) {
                entity.teleportTo((ServerLevel) level, lastBlockPos.x, lastBlockPos.y, lastBlockPos.z, RelativeMovement.ALL, entity.getYRot(), entity.getXRot());
            }
            entity.setDeltaMovement(lastDir.scale(Math.max(finalSpeed, 1f)));
        }
        entity.hurtMarked = true;

        entity.setPose(Pose.SWIMMING);

        TubeSoundManager.playTubeSuctionSound(entity, entity.position());

        if (!(entity instanceof Player player)) return;
        player.startFallFlying();
    }

    public static void changeDirection(MoveDirection direction, UUID entityUuid, Level level) {
        TravelPathMover travelPathMover = travelDataMap.get(entityUuid);
        if (travelPathMover == null) return;

        Direction junctionDirection = travelPathMover.getJunctionDirection();
        if (junctionDirection == null) return;

        Tuple<Direction, MoveDirection> directionTuple = JunctionDirectionUtils.resolveValidDirectionTuple(direction, travelPathMover.getLastPos(), level, junctionDirection);
        if (directionTuple == null) return;
        travelPathMover.setChosenDirection(directionTuple.getA());
    }

    public static void finishTravel(UUID entityUuid) {
        TravelPathMover pathMover = travelDataMap.get(entityUuid);
        if (pathMover == null) return;
        pathMover.setClientFinish();
    }

    public static void actionPointReach(UUID entityUuid, BlockPos blockPos) {
        TravelPathMover pathMover = travelDataMap.get(entityUuid);
        if (pathMover == null) return;
        pathMover.handleActionPoint(blockPos);
    }

    private static void handleServer(LivingEntity entity) {
        if (!travelDataMap.containsKey(entity.getUUID())) {
            if (!entity.getPersistentData().getBoolean(TRAVEL_TAG)) return;
            entity.getPersistentData().putBoolean(TRAVEL_TAG, false);
            return;
        }
        TravelPathMover travelPathData = travelDataMap.get(entity.getUUID());
        entity.resetFallDistance();
        travelPathData.tickEntity(entity);
    }

    public static boolean hasHyperTubeData(Entity entity) {
        return entity.getPersistentData().getBoolean(TRAVEL_TAG);
    }


    private static void syncPersistentData(LivingEntity entity) {
        PlayerSyncEvents.syncPlayerStateToAll(entity, true);
        if (entity instanceof ServerPlayer player)
            PacketDistributor.sendToPlayer(player, SyncPersistentDataPacket.create(entity));
    }
}