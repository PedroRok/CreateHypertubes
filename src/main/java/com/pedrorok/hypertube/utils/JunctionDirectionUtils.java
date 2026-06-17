package com.pedrorok.hypertube.utils;

import com.pedrorok.hypertube.blocks.HyperJunctionBlock;
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

    public static Tuple<Direction, MoveDirection> resolveValidDirectionTuple(MoveDirection direction, BlockPos blockPos, LevelAccessor level, Direction junctionDirection) {
        Direction candidate = direction.map(junctionDirection);

        BlockEntity junctionBlockEntity = level.getBlockEntity(blockPos);
        if (junctionBlockEntity != null) {
            BlockState junctionState = junctionBlockEntity.getBlockState();
            if (junctionState.getBlock() instanceof HyperJunctionBlock junctionBlock) {
                Direction entranceFace = junctionDirection.getOpposite();
                List<Direction> connectedFaces = junctionBlock.getConnectedFaces(junctionState);

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

    private static boolean isValidExit(List<Direction> connectedFaces, Direction candidate, Direction entranceFace) {
        return connectedFaces.stream()
                .anyMatch(face -> face == candidate && face != entranceFace);
    }
}