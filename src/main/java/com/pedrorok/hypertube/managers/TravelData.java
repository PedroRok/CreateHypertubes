package com.pedrorok.hypertube.managers;


import com.pedrorok.hypertube.blocks.HyperEntranceBlock;
import com.pedrorok.hypertube.blocks.HypertubeBlock;
import com.pedrorok.hypertube.blocks.TubeConnection;
import com.pedrorok.hypertube.blocks.blockentities.HypertubeBlockEntity;
import com.pedrorok.hypertube.managers.placement.BezierConnection;
import com.pedrorok.hypertube.managers.placement.SimpleConnection;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * @author Rok, Pedro Lucas nmm. Created on 25/04/2025
 * @project Create Hypertube
 */
public class TravelData {

    @Getter
    private final List<Vec3> travelPoints;
    private final List<UUID> bezierConnections;
    private final List<BlockPos> blockConnections;
    @Getter
    private final float speed;
    @Getter
    private int travelIndex;

    @Setter
    @Getter
    private Vec3 lastDir;

    @Getter
    @Setter
    private boolean isFinished = false;

    public TravelData(BlockPos firstPipe, Level level, BlockPos entrancePos, float speed) {
        this.travelPoints = new ArrayList<>();
        this.bezierConnections = new ArrayList<>();
        this.blockConnections = new ArrayList<>();
        this.speed = speed;
        travelPoints.add(entrancePos.getCenter());
        blockConnections.add(entrancePos);
        travelPoints.add(firstPipe.getCenter());
        blockConnections.add(firstPipe);

        addTravelPoint(firstPipe, level);
        checkAndRemoveNearPoints();
    }

    private void checkAndRemoveNearPoints() {
        if (travelPoints.size() < 2) return;

        Vec3 lastPoint = travelPoints.getFirst();
        for (int i = 1; i < travelPoints.size(); i++) {
            Vec3 currentPoint = travelPoints.get(i);
            double distance = lastPoint.distanceToSqr(currentPoint);
            if (distance < 0.8) {
                travelPoints.remove(i);
                i--;
                continue;
            }
            lastPoint = currentPoint;
        }


    }

    private void addTravelPoint(BlockPos pos, Level level) {
        BlockState blockState = level.getBlockState(pos);

        if (addCurvedTravelPoint(pos, level)) return;
        Block block = blockState.getBlock();
        if (!(block instanceof HypertubeBlock pipeBlock)) return;
        List<Direction> connectedFaces = pipeBlock.getConnectedFaces(blockState);
        for (Direction direction : connectedFaces) {
            BlockPos nextPipe = pos.relative(direction);
            if (blockConnections.contains(nextPipe)) continue;
            if (!(level.getBlockState(nextPipe).getBlock() instanceof TubeConnection connection)) continue;
            if (!connection.canTravelConnect(level, nextPipe, direction)
                && (level.getBlockEntity(nextPipe) instanceof HypertubeBlockEntity tubeEntity && !tubeEntity.isConnected())) continue;
            travelPoints.add(nextPipe.getCenter());
            blockConnections.add(nextPipe);
            addTravelPoint(nextPipe, level);
            break;
        }
    }


    private boolean addCurvedTravelPoint(BlockPos pos, Level level) {
        if (!(level.getBlockEntity(pos) instanceof HypertubeBlockEntity hypertubeBlockEntity)) return false;
        BezierConnection connection = hypertubeBlockEntity.getConnectionTo();
        boolean inverse = false;
        if (connection == null
            || bezierConnections.contains(connection.getUuid())) {
            SimpleConnection connectionFrom = hypertubeBlockEntity.getConnectionFrom();
            if (connectionFrom == null) {
                return false;
            }
            BlockEntity blockEntity = level.getBlockEntity(connectionFrom.pos());
            if (!(blockEntity instanceof HypertubeBlockEntity fromTube)) return false;
            BezierConnection fromTubeBezier = fromTube.getConnectionTo();
            if (fromTubeBezier == null) return false;
            if (bezierConnections.contains(fromTubeBezier.getUuid())) return false;
            connection = fromTubeBezier;
            inverse = true;
        }


        List<Vec3> bezierPoints = new ArrayList<>(connection.getBezierPoints());
        if (inverse) {
            Collections.reverse(bezierPoints);
        }
        bezierPoints.removeLast();
        bezierPoints.removeFirst();
        travelPoints.addAll(bezierPoints);
        bezierConnections.add(connection.getUuid());
        BlockPos toPos = connection.getToPos().pos();
        BlockPos fromPos = connection.getFromPos().pos();

        final BlockPos toPosFinal = inverse ? fromPos : toPos;
        final BlockPos fromPosFinal = inverse ? toPos : fromPos;

        if (!blockConnections.contains(toPosFinal))
            blockConnections.add(toPosFinal);
        if (!blockConnections.contains(fromPosFinal))
            blockConnections.add(fromPosFinal);
        addTravelPoint(toPosFinal, level);
        return true;
    }

    public Vec3 getTravelPoint() {
        if (travelIndex >= travelPoints.size()) return null;
        return travelPoints.get(travelIndex);
    }

    public void getNextTravelPoint() {
        if (travelIndex >= travelPoints.size()) return;
        travelIndex++;
    }

    public boolean hasNextTravelPoint() {
        return travelIndex + 1 < travelPoints.size();
    }

    public BlockPos getLastBlockPos() {
        if (blockConnections.isEmpty()) return null;
        return blockConnections.getLast();
    }
}