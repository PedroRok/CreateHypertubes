package com.pedrorok.hypertube.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;

/**
 * @author Rok, Pedro Lucas nmm. Created on 21/04/2025
 * @project Create Hypertube
 */
public interface TubeConnection {


    boolean canTravelConnect(LevelAccessor world, BlockPos pos, Direction facing);
}
