package com.pedrorok.hypertube.core.connection.interfaces;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * @author Rok, Pedro Lucas nmm. Created on 21/04/2025
 * @project Create Hypertube
 */
public interface ITubeConnection {
    boolean canTravelConnect(LevelAccessor world, BlockPos pos, Direction facing);

    default boolean isConnected(LevelAccessor world, BlockPos pos, Direction facing) {
        return world.getBlockState(pos.relative(facing)).getBlock() instanceof ITubeConnection;
    }

    List<Direction> getConnectedFaces(BlockState state);
}
