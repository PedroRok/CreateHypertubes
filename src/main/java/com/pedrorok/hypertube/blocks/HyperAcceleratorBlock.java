package com.pedrorok.hypertube.blocks;

import com.pedrorok.hypertube.blocks.blockentities.HyperAcceleratorBlockEntity;
import com.pedrorok.hypertube.blocks.blockentities.HyperEntranceBlockEntity;
import com.pedrorok.hypertube.core.connection.interfaces.ITubeActionPoint;
import com.pedrorok.hypertube.core.connection.interfaces.ITubeConnection;
import com.pedrorok.hypertube.core.connection.interfaces.ITubeConnectionEntity;
import com.pedrorok.hypertube.core.travel.TravelConstants;
import com.pedrorok.hypertube.core.travel.TravelPathMover;
import com.pedrorok.hypertube.network.packets.SpeedChangePacket;
import com.pedrorok.hypertube.registry.ModBlockEntities;
import com.pedrorok.hypertube.registry.ModBlocks;
import com.pedrorok.hypertube.utils.MessageUtils;
import com.pedrorok.hypertube.utils.TubeUtils;
import com.pedrorok.hypertube.utils.VoxelUtils;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.kinetics.base.KineticBlock;
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Rok, Pedro Lucas nmm. Created on 21/04/2025
 * @project Create Hypertube
 */
public class HyperAcceleratorBlock extends KineticBlock implements EntityBlock, ICogWheel, ITubeConnection, SimpleWaterloggedBlock, ITubeActionPoint {

    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;

    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public HyperAcceleratorBlock(Properties properties) {
        super(properties);
        registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(OPEN, false)
                .setValue(WATERLOGGED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, OPEN, WATERLOGGED);
        super.createBlockStateDefinition(builder);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Player player = context.getPlayer();
        FluidState fluidstate = context.getLevel().getFluidState(context.getClickedPos());
        if (player == null) {
            return this.defaultBlockState()
                    .setValue(FACING, context.getClickedFace().getOpposite())
                    .setValue(OPEN, false)
                    .setValue(WATERLOGGED, fluidstate.is(Fluids.WATER));
        }
        Direction direction = player.getDirection();
        if (player.getXRot() < -45) {
            direction = Direction.UP;
        } else if (player.getXRot() > 45) {
            direction = Direction.DOWN;
        }
        return this.defaultBlockState()
                .setValue(FACING, direction)
                .setValue(OPEN, false)
                .setValue(WATERLOGGED, fluidstate.is(Fluids.WATER));
    }

    @Override
    public @NotNull BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public @NotNull BlockState mirror(BlockState state, Mirror mirror) {
        return state.setValue(FACING, mirror.mirror(state.getValue(FACING)));
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return false;
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(FACING).getAxis();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
        return ModBlockEntities.HYPER_ACCELERATOR.get().create(blockPos, blockState);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        return (level1, pos, state1, be) -> ((HyperAcceleratorBlockEntity) be).tick();
    }

    public boolean canTravelConnect(LevelAccessor world, BlockPos pos, Direction facing) {
        BlockState state = world.getBlockState(pos);
        return facing.getOpposite() == state.getValue(FACING)
               && state.getBlock() instanceof HyperAcceleratorBlock;
    }

    @Override
    public List<Direction> getConnectedFaces(BlockState state) {
        return new ArrayList<>(List.of(state.getValue(FACING).getOpposite(), state.getValue(FACING)));
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        return InteractionResult.SUCCESS;
    }

    protected @NotNull BlockState updateShape(BlockState p_313906_, @NotNull Direction p_313739_, @NotNull BlockState p_313829_, @NotNull LevelAccessor p_313692_, @NotNull BlockPos p_313842_, @NotNull BlockPos p_313843_) {
        if (p_313906_.getValue(WATERLOGGED)) {
            p_313692_.scheduleTick(p_313842_, Fluids.WATER, Fluids.WATER.getTickDelay(p_313692_));
        }
        return super.updateShape(p_313906_, p_313739_, p_313829_, p_313692_, p_313842_, p_313843_);
    }

    protected @NotNull FluidState getFluidState(BlockState p_313789_) {
        return p_313789_.getValue(WATERLOGGED) ? Fluids.WATER.getSource(true) : super.getFluidState(p_313789_);
    }

    @Override
    public @NotNull BlockState playerWillDestroy(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState
            state, @NotNull Player player) {
        return playerWillDestroy(level, pos, state, player, false);
    }

    private BlockState playerWillDestroy(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState
            state, @NotNull Player player, boolean wrenched) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof ITubeConnectionEntity tube))
            return super.playerWillDestroy(level, pos, state, player);

        int toDrop = tube.blockBroken();

        if (!player.isCreative()) {
            if (toDrop != 0 || wrenched) {
                ItemStack stack = new ItemStack(ModBlocks.HYPERTUBE.get(), toDrop + (wrenched ? 1 : 0));
                if (wrenched)
                    player.getInventory().placeItemBackInInventory(stack);
                else
                    Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack);
            }
        }

        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public void handleTravelPath(LivingEntity entity, TravelPathMover mover, BlockPos pos) {
        Level level = entity.level();
        HyperAcceleratorBlockEntity tube = (HyperAcceleratorBlockEntity) level.getBlockEntity(pos);
        if (tube == null) return;
        float speed = TubeUtils.calculateTravelSpeed(tube.getSpeed()) / 2;
        float newSpeed = mover.getTravelSpeed() + speed;
        mover.setTravelSpeed(newSpeed);
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, new SpeedChangePacket(entity.getId(), newSpeed));
    }

    // ------- Collision Shapes -------
    public VoxelShape getShape(BlockState state, @Nullable CollisionContext ctx) {
        if (ctx instanceof EntityCollisionContext ecc
            && ecc.getEntity() != null
            && ecc.getEntity().getPersistentData().getBoolean(TravelConstants.TRAVEL_TAG)) {
            return VoxelUtils.empty();
        }
        return Shapes.block();
    }

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
    // ------- Collision Shapes -------

    @Override
    public boolean isSmallCog() {
        return true;
    }
}
