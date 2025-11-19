package com.pedrorok.hypertube.blocks;

import com.pedrorok.hypertube.blocks.blockentities.ActionTubeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.jetbrains.annotations.NotNull;

/**
 * @author Rok, Pedro Lucas nmm. Created on 19/11/2025
 * @project Create Hypertube
 */
public abstract class ActionTubeBlock extends TubeBlock {

    public static final IntegerProperty POWER = BlockStateProperties.POWER;

    public ActionTubeBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected boolean isSignalSource(@NotNull BlockState state) {
        return super.isSignalSource(state);
    }

    @Override
    protected int getDirectSignal(@NotNull BlockState blockState, @NotNull BlockGetter blockAccess, @NotNull BlockPos pos, @NotNull Direction side) {
        return getSignal(blockState, blockAccess, pos, side);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);

        int currentPower = state.getValue(POWER);

        if (currentPower > 0) {
            level.setBlock(pos, state.setValue(POWER, 0), 3);
            level.updateNeighborsAt(pos, this);
        }
    }

    @Override
    protected int getSignal(@NotNull BlockState blockState, @NotNull BlockGetter blockAccess, @NotNull BlockPos pos, @NotNull Direction side) {
        if (blockAccess.getBlockEntity(pos) instanceof ActionTubeBlockEntity action) {
            if (!action.hasSmartTubeAttachment(side.getOpposite())) return 0;
        }
        return blockState.getValue(POWER);
    }
}
