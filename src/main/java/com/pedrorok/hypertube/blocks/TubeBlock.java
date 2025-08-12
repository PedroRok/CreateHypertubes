package com.pedrorok.hypertube.blocks;

import com.pedrorok.hypertube.core.connection.interfaces.ITubeConnection;
import com.pedrorok.hypertube.core.connection.interfaces.ITubeConnectionEntity;
import com.pedrorok.hypertube.registry.ModBlocks;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.kinetics.base.KineticBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.BlockEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author Rok, Pedro Lucas nmm. Created on 09/08/2025
 * @project Create Hypertube
 */
public abstract class TubeBlock extends KineticBlock implements ITubeConnection, SimpleWaterloggedBlock {
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public TubeBlock(Properties properties) {
        super(properties);
    }

    // ---- Shape ----
    public abstract VoxelShape getShape(BlockState state, @Nullable CollisionContext ctx);

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter worldIn, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return getShape(state, context);
    }

    @Override
    public @NotNull VoxelShape getCollisionShape(@NotNull BlockState state, @NotNull BlockGetter worldIn, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return getShape(state, context);
    }

    @Override
    public @NotNull VoxelShape getBlockSupportShape(@NotNull BlockState state, @NotNull BlockGetter reader, @NotNull BlockPos pos) {
        return getShape(state);
    }

    @Override
    public @NotNull VoxelShape getInteractionShape(@NotNull BlockState state, @NotNull BlockGetter worldIn, @NotNull BlockPos pos) {
        return getShape(state);
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    public VoxelShape getShape(BlockState state) {
        return getShape(state, null);
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return null;
    }


    // ---- Waterlogging ----
    @Override
    @NotNull
    public BlockState updateShape(BlockState p_313906_, @NotNull Direction p_313739_, @NotNull BlockState p_313829_, @NotNull LevelAccessor p_313692_, @NotNull BlockPos p_313842_, @NotNull BlockPos p_313843_) {
        if (p_313906_.getValue(WATERLOGGED)) {
            p_313692_.scheduleTick(p_313842_, Fluids.WATER, Fluids.WATER.getTickDelay(p_313692_));
        }
        return super.updateShape(p_313906_, p_313739_, p_313829_, p_313692_, p_313842_, p_313843_);
    }

    @Override
    public @NotNull FluidState getFluidState(BlockState p_313789_) {
        return p_313789_.getValue(WATERLOGGED) ? Fluids.WATER.getSource(true) : super.getFluidState(p_313789_);
    }

    // ---- Item ----
    public abstract Item getItem();

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
        return getItem().getDefaultInstance();
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter p_49823_, BlockPos p_49824_, BlockState p_49825_) {
        return getItem().getDefaultInstance();
    }


    // ---- Destroying ----
    @Override
    public @NotNull void playerWillDestroy(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull Player player) {
        playerWillDestroy(level, pos, state, player, false);
    }

    void playerWillDestroy(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull Player player, boolean wrenched) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof ITubeConnectionEntity tube)) {
            super.playerWillDestroy(level, pos, state, player);
            return;
        }

        int toDrop = tube.blockBroken();

        if (wrenched) {
            if (this instanceof HypertubeBlock) {
                toDrop += 1;
            } else {
                player.getInventory().placeItemBackInInventory(getItem().getDefaultInstance());
            }
        }

        if (!player.isCreative()) {
            if (toDrop != 0 || wrenched) {
                ItemStack stack = new ItemStack(ModBlocks.HYPERTUBE.get(), toDrop);
                if (wrenched) player.getInventory().placeItemBackInInventory(stack);
                else Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack);
            }
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public InteractionResult onSneakWrenched(BlockState state, UseOnContext context) {
        if (context.getPlayer() == null) return InteractionResult.PASS;
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();

        playerWillDestroy(world, context.getClickedPos(), state, context.getPlayer(), true);

        if (!(world instanceof ServerLevel)) return InteractionResult.SUCCESS;

        BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(world, pos, world.getBlockState(pos), player);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.isCanceled()) return InteractionResult.SUCCESS;

        world.destroyBlock(pos, false);
        playRemoveSound(world, pos);
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean canTravelConnect(LevelAccessor world, BlockPos posSelf, Direction facing) {
        BlockPos relative = posSelf.relative(facing);
        BlockState otherState = world.getBlockState(relative);
        Block block = otherState.getBlock();
        return block instanceof ITubeConnection
               && (!(block instanceof TubeBlock hypertubeBlock)
                   || canOtherConnectTo(otherState, hypertubeBlock, facing));
    }

    private boolean canOtherConnectTo(BlockState otherState, TubeBlock otherTube, Direction
            facing) {
        List<Direction> connectedFaces = otherTube.getConnectedFaces(otherState);
        return connectedFaces.isEmpty() || connectedFaces.contains(facing);
    }
}
