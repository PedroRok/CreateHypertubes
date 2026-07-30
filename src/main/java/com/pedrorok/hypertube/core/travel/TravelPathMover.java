package com.pedrorok.hypertube.core.travel;

import com.mojang.datafixers.util.Pair;
import com.pedrorok.hypertube.blocks.blockentities.parent.ActionTubeBlockEntity;
import com.pedrorok.hypertube.core.compat.Mods;
import com.pedrorok.hypertube.core.compat.sable.SableCompat;
import com.pedrorok.hypertube.core.connection.interfaces.ITubeActionPoint;
import com.pedrorok.hypertube.core.data.MoveDirection;
import com.pedrorok.hypertube.network.packets.EntityTravelDirDataPacket;
import com.pedrorok.hypertube.network.packets.SyncEntityPosPacket;
import com.pedrorok.hypertube.utils.JunctionDirectionUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * @author Rok, Pedro Lucas nmm. Created on 03/07/2025
 * @project Create Hypertube
 */
public class TravelPathMover {
    private final ArrayList<Vec3> pathPoints;
    private final Set<BlockPos> actionPoints;
    private final Set<BlockPos> activeActionPoints;
    @Getter
    @Setter
    private float travelSpeed;
    private final Consumer<EndTravelData> onFinishCallback;
    @Getter
    private final BlockPos lastPos;

    // junction data
    private final boolean isJunction;
    private Direction chosenDirection = Direction.NORTH;
    @Getter
    private Direction junctionDirection;
    //

    private int currentSegment = 0;
    private Vec3 currentStart;
    private Vec3 currentEnd;
    private double totalDistance;
    private double traveled;

    private boolean finished = false;

    private Vec3 lastDirection;

    public TravelPathMover(BlockEntity entrance, TravelPathData data, LivingEntity entity, float travelSpeed, Consumer<EndTravelData> onFinishCallback) {
        this.pathPoints = data.getTravelPoints();
        this.actionPoints = data.getActionPoints();
        this.activeActionPoints = new HashSet<>() {{
            add(entrance.getBlockPos());
        }};
        this.lastPos = data.getLastBlockPos();
        actionPoints.add(this.lastPos);
        this.travelSpeed = travelSpeed;

        this.currentStart = entity.position();
        this.currentEnd = pathPoints.getFirst().subtract(0, 0.25, 0);

        double pathLength = getPathLength();
        if (this.currentStart.distanceToSqr(this.currentEnd) > pathLength * pathLength) {
            this.currentStart = this.currentEnd;
        }

        this.totalDistance = currentStart.distanceTo(currentEnd);
        this.traveled = 0;

        this.onFinishCallback = onFinishCallback;

        this.isJunction = data.isFinishWithJunction();
        this.junctionDirection = data.getJunctionDirection();

        if (isJunction) {
            this.chosenDirection = resolveDefaultDirection(entity.level());
            return;
        }
        this.lastDirection = data.getEndDirection(entity.level());
        if (lastDirection == null) {
            this.lastDirection = pathPoints.getLast().subtract(pathPoints.get(pathPoints.size() - 2)).normalize();
        }
        this.pathPoints.add(pathPoints.getLast().add(this.lastDirection.scale(1)));
    }

    private Direction resolveDefaultDirection(Level level) {
        if (junctionDirection == null || lastPos == null) return chosenDirection;
        MoveDirection moveDirection = MoveDirection.RIGHT;
        do {
            Tuple<Direction, MoveDirection> directionTuple =
                    JunctionDirectionUtils.resolveValidDirectionTuple(moveDirection, lastPos, level, junctionDirection);
            if (directionTuple != null) return directionTuple.getA();
            moveDirection = moveDirection.getNext();
        } while (moveDirection != MoveDirection.RIGHT);
        return chosenDirection;
    }

    private double getPathLength() {
        double length = 0;
        for (int i = 1; i < pathPoints.size(); i++) {
            length += pathPoints.get(i - 1).distanceTo(pathPoints.get(i));
        }
        return length;
    }

    @SuppressWarnings("D")
    public void tickEntity(LivingEntity entity) {
        if (entity.isSpectator() || !entity.isAlive()) {
            onFinishCallback.accept(EndTravelData.forced(entity, isJunction, chosenDirection));
            return;
        }

        while (!activeActionPoints.isEmpty()) {
            BlockPos actionPos = activeActionPoints.iterator().next();
            activeActionPoints.remove(actionPos);
            Block block = entity.level().getBlockState(actionPos).getBlock();
            if (block instanceof ITubeActionPoint travelAction) {
                travelAction.handleTravelPath(entity, this, actionPos);
            }
            BlockEntity be = entity.level().getBlockEntity(actionPos);
            if (be instanceof ActionTubeBlockEntity actionTubeBlockEntity && actionTubeBlockEntity.hasAnyTubeAttachment()) {
                actionTubeBlockEntity.activateAllTubeAttachments(entity, this, actionPos);
            }
        }

        if (finished) {
            onFinishCallback.accept(EndTravelData.normal(entity, isJunction, chosenDirection));
            return;
        }

        double remaining = travelSpeed;
        while (remaining >= totalDistance - traveled) {
            remaining -= totalDistance - traveled;
            currentSegment++;
            if (currentSegment >= pathPoints.size()) {
                onFinishCallback.accept(EndTravelData.normal(entity, isJunction, chosenDirection));
                return;
            }
            currentStart = currentEnd;
            currentEnd = pathPoints.get(currentSegment).subtract(0, 0.25, 0);
            totalDistance = currentStart.distanceTo(currentEnd);
            traveled = 0;
        }
        traveled += remaining;

        Pair<Vec3, Vec3> posDir = Pair.of(currentStart.lerp(currentEnd, traveled / totalDistance),
                currentEnd.subtract(currentStart).normalize());
        posDir = Mods.SABLE.executeIfInstalled(() -> (pd) -> SableCompat.transformToWorld(entity.level(), pd.getFirst(), pd.getSecond()), posDir);

        Vec3 newPos = posDir.getFirst();

        entity.moveTo(newPos.x, newPos.y, newPos.z);

        entity.resetFallDistance();

        handleEntityDirection(entity, posDir.getSecond());
        if (entity instanceof Player player) {
            if (player.isFallFlying())
                player.stopFallFlying();
            return;
        }
        PacketDistributor.sendToPlayersTrackingEntity(entity, SyncEntityPosPacket.create(entity, currentSegment));
    }

    public void handleActionPoint(BlockPos actionPos) {
        activeActionPoints.add(actionPos);
        actionPoints.remove(actionPos);
    }

    public void setChosenDirection(Direction direction) {
        this.chosenDirection = direction;
        this.lastDirection = Vec3.atLowerCornerOf(chosenDirection.getNormal());
        this.pathPoints.removeLast();
        this.pathPoints.add(pathPoints.getLast().add(this.lastDirection));
    }


    private static void handleEntityDirection(LivingEntity entity, Vec3 direction) {
        float yaw = (float) Math.toDegrees(Math.atan2(-direction.x, direction.z));
        float pitch = (float) Math.toDegrees(Math.atan2(-direction.y, Math.sqrt(direction.x * direction.x + direction.z * direction.z)));
        entity.setYRot(yaw);
        entity.setXRot(pitch);
        if (entity.level().isClientSide) return;
        PacketDistributor.sendToPlayersTrackingEntity(entity, EntityTravelDirDataPacket.create(entity));
    }

    public Vec3 getLastDir() {
        return lastDirection;
    }

    public void setClientFinish() {
        finished = true;
    }
}