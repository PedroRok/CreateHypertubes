package com.pedrorok.hypertube.blocks;

import com.pedrorok.hypertube.blocks.blockentities.parent.ActionTubeBlockEntity;
import com.pedrorok.hypertube.core.smarttube.ITubeAttachment;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
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

    protected abstract BooleanProperty propertyToUpdate();


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

    public static boolean hasSignalOnSide(Level level, BlockPos pos, Direction side) {
        return level.getSignal(pos.relative(side), side) > 0;
    }

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter world, BlockPos pos, Direction side) {
        ActionTubeBlockEntity tubeBlockEntity = (ActionTubeBlockEntity) world.getBlockEntity(pos);
        if (tubeBlockEntity == null) return false;
        return canPlaceAttachment(state, world, pos, side) && tubeBlockEntity.getAttachmentDirections().contains(side.getOpposite());
    }

    public boolean canPlaceAttachment(BlockState state, BlockGetter world, BlockPos pos, Direction side) {
        return side != null && side != state.getValue(FACING) && side != state.getValue(FACING).getOpposite();
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

    @Override
    void dropBlockToPlayer(boolean isWrenched, Level level, BlockPos pos, Player player, BlockEntity blockEntity, int amount) {
        super.dropBlockToPlayer(isWrenched, level, pos, player, blockEntity, amount);
        if (player.isCreative()) return;
        if (!(blockEntity instanceof ActionTubeBlockEntity actionTubeBlock)) return;
        actionTubeBlock.getTubeAttachments().forEach((dir, attachment) -> {
            ItemStack stack = attachment.getItemStack();
            if (isWrenched) player.getInventory().placeItemBackInInventory(stack);
            else Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack);
        });
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {

        BlockPos clickedPos = context.getClickedPos();
        Direction clickedFace = context.getClickedFace();
        Level level = context.getLevel();
        BlockEntity blockEntity = level.getBlockEntity(clickedPos);

        if (!(blockEntity instanceof ActionTubeBlockEntity action)) return InteractionResult.PASS;
        if (!action.hasTubeAttachment(clickedFace)) return InteractionResult.PASS;



        ITubeAttachment iTubeAttachment = action.removeTubeAttachment(clickedFace);
        if (iTubeAttachment == null) return InteractionResult.SUCCESS;
        Player player = context.getPlayer();
        if (!player.isCreative()) {
            ItemStack stack = iTubeAttachment.getItemStack();
            player.getInventory().placeItemBackInInventory(stack);
        }
        IWrenchable.playRemoveSound(context.getLevel(), context.getClickedPos());
        return InteractionResult.SUCCESS;
    }
}
