package com.pedrorok.hypertube.utils;

import com.pedrorok.hypertube.blocks.HyperJunctionBlock;
import com.pedrorok.hypertube.core.data.MoveDirection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author Rok, Pedro Lucas nmm. 16/06/2026
 * @project Create Hypertube
 */
public final class JunctionDirectionUtils {

    private JunctionDirectionUtils() {
    }

    @SuppressWarnings("D")
    public static Tuple<Direction, MoveDirection> resolveValidDirectionTuple(MoveDirection direction, BlockPos blockPos, LevelAccessor level, Direction junctionDirection) {
        Direction candidate = direction.map(junctionDirection);

        BlockEntity junctionBlockEntity = level.getBlockEntity(blockPos);
        if (junctionBlockEntity != null) {
            BlockState junctionState = junctionBlockEntity.getBlockState();
            if (junctionState.getBlock() instanceof HyperJunctionBlock junctionBlock) {
                Direction entranceFace = junctionDirection.getOpposite();

                List<Direction> connectedFaces = getConnectedFaces(junctionState, entranceFace, junctionBlock);

                boolean isValidExit = isValidExit(connectedFaces, candidate, entranceFace);
                if (!isValidExit) {
                    boolean frontIsValid = isValidExit(connectedFaces, junctionDirection, entranceFace);
                    if (!frontIsValid) return null;
                    candidate = junctionDirection;
                }
            }
        }
        return new Tuple<>(candidate, MoveDirection.fromDirections(junctionDirection, candidate));
    }

    public static List<Direction> getConnectedFaces(BlockState junctionState, @Nullable Direction playerEntranceFace, HyperJunctionBlock junctionBlock) {
        Direction junctionBlockDirection = junctionState.getValue(HyperJunctionBlock.FACING);
        return switch (junctionState.getValue(HyperJunctionBlock.JUNCTION_MODE)) {
            case FORCED_CONTINUE -> {
                if (playerEntranceFace == junctionBlockDirection) {
                    yield List.of(junctionBlockDirection.getCounterClockWise());
                }
                yield List.of(junctionBlockDirection.getClockWise(), junctionBlockDirection.getCounterClockWise());
            }
            case FORCED_CENTER -> {
                if (playerEntranceFace != junctionBlockDirection) {
                    yield List.of(junctionBlockDirection);
                }
                yield List.of(junctionBlockDirection.getClockWise());
            }
            case AUTOMATIC -> junctionBlock.getConnectedFaces(junctionState);
        };
    }

    private static boolean isValidExit(List<Direction> connectedFaces, Direction candidate, Direction entranceFace) {
        return connectedFaces.stream()
                .anyMatch(face -> face == candidate && face != entranceFace);
    }
}