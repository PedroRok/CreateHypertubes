package com.pedrorok.hypertube.core.travel;

import com.pedrorok.hypertube.blocks.HyperJunctionBlock;
import com.pedrorok.hypertube.blocks.blockentities.parent.ActionTubeBlockEntity;
import com.pedrorok.hypertube.blocks.blockentities.parent.TubeBlockEntity;
import com.pedrorok.hypertube.core.connection.BezierConnection;
import com.pedrorok.hypertube.core.connection.SimpleConnection;
import com.pedrorok.hypertube.core.connection.interfaces.IConnection;
import com.pedrorok.hypertube.core.connection.interfaces.ITubeActionPoint;
import com.pedrorok.hypertube.core.connection.interfaces.ITubeConnection;
import com.pedrorok.hypertube.core.connection.interfaces.ITubeConnectionEntity;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.*;

/**
 * @author Rok, Pedro Lucas nmm. Created on 25/04/2025
 * @project Create Hypertube
 */
@SuppressWarnings("D")
public class TravelPathData {

    @Getter
    private final ArrayList<Vec3> travelPoints; //
    private final List<UUID> bezierConnections;
    private final List<BlockPos> blockConnections;
    @Getter
    private final Set<BlockPos> actionPoints;
    private final Direction facingDirection;

    @Getter
    private boolean finishWithJunction = false;
    @Getter
    private Direction junctionDirection;

    public TravelPathData(Direction facingDirection, Level level, BlockPos entrancePos) {
        this.travelPoints = new ArrayList<>();
        this.bezierConnections = new ArrayList<>();
        this.blockConnections = new ArrayList<>();
        this.actionPoints = new HashSet<>();
        this.facingDirection = facingDirection;
        travelPoints.add(entrancePos.getCenter());
        blockConnections.add(entrancePos);

        BlockPos firstPipe = entrancePos.relative(facingDirection);
        travelPoints.add(firstPipe.getCenter());
        blockConnections.add(firstPipe);
        addTravelPoint(entrancePos, level, true, facingDirection);
        addTravelPoint(firstPipe, level, facingDirection);
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

    private void addTravelPoint(BlockPos pos, Level level, Direction connectingFrom) {
        addTravelPoint(pos, level, false, connectingFrom);
    }

    private void addTravelPoint(BlockPos pos, Level level, boolean entrance, Direction connectingFrom) {
        BlockState blockState = level.getBlockState(pos);
        if (level.getBlockState(pos).getBlock() instanceof ITubeActionPoint ||
                (level.getBlockEntity(pos) instanceof ActionTubeBlockEntity tubeEntity && tubeEntity.hasAnyTubeAttachment())) {
            actionPoints.add(pos);
        }

        if (blockState.getBlock() instanceof HyperJunctionBlock
                && level.getBlockEntity(pos) instanceof TubeBlockEntity tubeBlockEntity
                && tubeBlockEntity.getConnections().size() > 2 && !entrance) {
            blockConnections.add(pos);
            travelPoints.add(pos.getCenter());
            junctionDirection = connectingFrom;
            finishWithJunction = true;
            return;
        }


        if (addCurvedTravelPoint(pos, level, entrance)) return;
        Block block = blockState.getBlock();
        if (!(block instanceof ITubeConnection pipeBlock)) return;
        List<Direction> connectedFaces = pipeBlock.getConnectedFaces(blockState);
        for (Direction direction : connectedFaces) {
            BlockPos nextPipe = pos.relative(direction);
            if (blockConnections.contains(nextPipe)) continue;
            if (!(level.getBlockState(nextPipe).getBlock() instanceof ITubeConnection connection)) continue;
            if (!connection.canTravelConnect(level, nextPipe, direction)
                    && (level.getBlockEntity(nextPipe) instanceof ITubeConnectionEntity tubeEntity && !tubeEntity.isConnected()))
                continue;
            travelPoints.add(nextPipe.getCenter());
            blockConnections.add(nextPipe);
            addTravelPoint(nextPipe, level, direction);
            break;
        }
    }


    private boolean addCurvedTravelPoint(BlockPos pos, Level level, boolean entrance) {
        if (!(level.getBlockEntity(pos) instanceof ITubeConnectionEntity hypertubeBlockEntity)) return false;
        boolean connected = false;
        List<IConnection> connections = hypertubeBlockEntity.getConnections();

        if (entrance) {
            IConnection connectionInDirection = hypertubeBlockEntity.getConnectionInDirection(facingDirection);
            if (connectionInDirection != null) {
                connections = List.of(connectionInDirection);
            }
        }

        for (IConnection connection : connections) {
            BezierConnection bezier;
            boolean inverse = false;
            BlockPos currentFromPos;

            if (connection instanceof SimpleConnection simple) {
                BlockEntity blockEntity = level.getBlockEntity(simple.pos());
                if (!(blockEntity instanceof ITubeConnectionEntity fromTube)) continue;
                IConnection fromTubeConn = fromTube.getThisConnectionFrom(simple);
                if (!(fromTubeConn instanceof BezierConnection fromTubeBezier)) continue;
                bezier = fromTubeBezier;
                inverse = true;
                currentFromPos = simple.pos();
            } else {
                if (!(connection instanceof BezierConnection bezierConnection)) continue;
                bezier = bezierConnection;
                currentFromPos = pos;
            }
            if (bezierConnections.contains(bezier.getUuid())) continue;

            List<Vec3> bezierPoints = new ArrayList<>(bezier.getBezierPoints(level, currentFromPos));
            if (inverse) {
                Collections.reverse(bezierPoints);
            }

            Direction entranceDirectionForToPosFinal = null;
            if (bezierPoints.size() >= 2) {
                Vec3 secondToLast = bezierPoints.get(bezierPoints.size() - 2);
                Vec3 last = bezierPoints.get(bezierPoints.size() - 1);
                Vec3 arrivalVector = last.subtract(secondToLast);
                if (arrivalVector.lengthSqr() > 0.01) {
                    entranceDirectionForToPosFinal = Direction.getNearest(arrivalVector.x, arrivalVector.y, arrivalVector.z);
                }
            }

            bezierPoints.removeLast();
            bezierPoints.removeFirst();
            travelPoints.addAll(bezierPoints);
            bezierConnections.add(bezier.getUuid());

            BlockPos storedFromPos = bezier.getFromPos().pos();
            SimpleConnection toConnection = bezier.getToPos();
            if (toConnection == null) continue;
            BlockPos storedToPos = toConnection.pos();
            BlockPos offset = storedToPos.subtract(storedFromPos);
            BlockPos currentToPos = currentFromPos.offset(offset);

            final BlockPos toPosFinal = inverse ? currentFromPos : currentToPos;
            final BlockPos fromPosFinal = inverse ? currentToPos : currentFromPos;

            if (!blockConnections.contains(fromPosFinal)) {
                blockConnections.add(fromPosFinal);
                if (level.getBlockState(fromPosFinal).getBlock() instanceof ITubeActionPoint ||
                        (level.getBlockEntity(fromPosFinal) instanceof ActionTubeBlockEntity tubeEntity && tubeEntity.hasAnyTubeAttachment())) {
                    actionPoints.add(fromPosFinal);
                }
            }
            if (!blockConnections.contains(toPosFinal)) {
                blockConnections.add(toPosFinal);
                if (level.getBlockState(toPosFinal).getBlock() instanceof ITubeActionPoint ||
                        (level.getBlockEntity(toPosFinal) instanceof ActionTubeBlockEntity tubeEntity && tubeEntity.hasAnyTubeAttachment())) {
                    actionPoints.add(toPosFinal);
                }
            }

            addTravelPoint(toPosFinal, level, entranceDirectionForToPosFinal);
            connected = true;
            break;
        }
        return connected;
    }

    public BlockPos getLastBlockPos() {
        if (blockConnections.isEmpty()) return null;
        return blockConnections.getLast();
    }

    public Vec3 getEndDirection(Level level) {
        if (blockConnections.isEmpty()) return null;
        BlockEntity blockEntity = level.getBlockEntity(blockConnections.getLast());
        if (blockEntity instanceof ITubeConnectionEntity connection) {
            return connection.getExitDirection();
        }
        return null;
    }
}