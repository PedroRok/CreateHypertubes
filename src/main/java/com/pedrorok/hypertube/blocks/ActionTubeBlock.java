package com.pedrorok.hypertube.blocks;

import com.pedrorok.hypertube.blocks.blockentities.ActionTubeBlockEntity;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author Rok, Pedro Lucas nmm. Created on 19/11/2025
 * @project Create Hypertube
 */
public abstract class ActionTubeBlock extends TubeBlock {

    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final IntegerProperty POWER = BlockStateProperties.POWER;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;


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
        if (!(blockAccess.getBlockEntity(pos) instanceof ActionTubeBlockEntity action)) {
            return 0;
        }
        if (!action.hasTubeAttachment(side.getOpposite()) || !action.canEmitTo(side.getOpposite())) return 0;

        return blockState.getValue(POWER);
    }

    protected boolean getSignalSide(Level level, BlockPos pos, List<Direction> checkSides) {
        for (Direction direction : checkSides) {
            if (level.getSignal(pos.relative(direction), direction) > 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter world, BlockPos pos, Direction side) {
        ActionTubeBlockEntity tubeBlockEntity = (ActionTubeBlockEntity) world.getBlockEntity(pos);
        if (tubeBlockEntity == null) return false;
        return side != null && side != state.getValue(FACING) && side != state.getValue(FACING).getOpposite() && tubeBlockEntity.getAttachmentDirections().contains(side);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, @NotNull BlockPos pos, @NotNull Block block, @NotNull BlockPos fromPos, boolean isMoving) {
        ActionTubeBlockEntity tubeBlockEntity = (ActionTubeBlockEntity) level.getBlockEntity(pos);
        if (tubeBlockEntity == null) return;
        boolean neighborHasSignal = getSignalSide(level, pos, tubeBlockEntity.getAttachmentDirectionsNoEmit());
        boolean actualState = state.getValue(POWERED);
        if (neighborHasSignal && !actualState) {
            level.scheduleTick(pos, this, 4);
            level.setBlock(pos, state.setValue(POWERED, true).setValue(propertyToUpdate(), !state.getValue(propertyToUpdate())), 2);
            IWrenchable.playRotateSound(level, pos);

        } else if (!neighborHasSignal && actualState) {
            level.setBlock(pos, state.setValue(POWERED, false).setValue(propertyToUpdate(), !state.getValue(propertyToUpdate())), 2);
            IWrenchable.playRotateSound(level, pos);
        }
    }

    protected abstract BooleanProperty propertyToUpdate();


    public boolean canPlaceAttachment(BlockState state, BlockGetter world, BlockPos pos, Direction side) {
        return side != null && side != state.getValue(FACING) && side != state.getValue(FACING).getOpposite();
    }
}
