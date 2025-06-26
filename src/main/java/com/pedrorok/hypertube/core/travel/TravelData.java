package com.pedrorok.hypertube.core.travel;

import com.pedrorok.hypertube.blocks.HypertubeBlock;
import com.pedrorok.hypertube.core.connection.BezierConnection;
import com.pedrorok.hypertube.core.connection.SimpleConnection;
import com.pedrorok.hypertube.core.connection.interfaces.IConnection;
import com.pedrorok.hypertube.core.connection.interfaces.TubeConnection;
import com.pedrorok.hypertube.blocks.blockentities.HypertubeBlockEntity;
import com.pedrorok.hypertube.core.connection.interfaces.TubeConnectionEntity;
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

    public TravelData(float speed) {
        this.travelPoints = new ArrayList<>();
        this.bezierConnections = new ArrayList<>();
        this.blockConnections = new ArrayList<>();
        this.speed = speed;
        this.lastDir = Vec3.ZERO;
    }

    public void init(BlockPos firstPipe, Level level, BlockPos entrancePos) {
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
                && (level.getBlockEntity(nextPipe) instanceof HypertubeBlockEntity tubeEntity && !tubeEntity.isConnected()))
                continue;
            travelPoints.add(nextPipe.getCenter());
            blockConnections.add(nextPipe);
            addTravelPoint(nextPipe, level);
            break;
        }
    }


    private boolean addCurvedTravelPoint(BlockPos pos, Level level) {
        if (!(level.getBlockEntity(pos) instanceof TubeConnectionEntity hypertubeBlockEntity)) return false;
        boolean connected = false;
        for (IConnection connection : hypertubeBlockEntity.getConnections()) {
            BezierConnection bezier = null;
            boolean inverse = false;
            if (connection instanceof SimpleConnection simple) {
                BlockEntity blockEntity = level.getBlockEntity(simple.pos());
                if (!(blockEntity instanceof TubeConnectionEntity fromTube)) continue;
                IConnection fromTubeConn = fromTube.getThisConnectionFrom(simple);
                if (!(fromTubeConn instanceof BezierConnection fromTubeBezier)) continue;
                bezier = fromTubeBezier;
                inverse = true;
            } else {
                if (!(connection instanceof BezierConnection bezierConnection)) continue;
                bezier = bezierConnection;
            }
            if (bezierConnections.contains(bezier.getUuid())) continue;

            List<Vec3> bezierPoints = new ArrayList<>(bezier.getBezierPoints());
            if (inverse) {
                Collections.reverse(bezierPoints);
            }
            bezierPoints.removeLast();
            bezierPoints.removeFirst();
            travelPoints.addAll(bezierPoints);
            bezierConnections.add(bezier.getUuid());
            BlockPos toPos = bezier.getToPos().pos();
            BlockPos fromPos = bezier.getFromPos().pos();

            final BlockPos toPosFinal = inverse ? fromPos : toPos;
            final BlockPos fromPosFinal = inverse ? toPos : fromPos;

            if (!blockConnections.contains(toPosFinal))
                blockConnections.add(toPosFinal);
            if (!blockConnections.contains(fromPosFinal))
                blockConnections.add(fromPosFinal);
            addTravelPoint(toPosFinal, level);
            connected = true;
        }
        return connected;
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