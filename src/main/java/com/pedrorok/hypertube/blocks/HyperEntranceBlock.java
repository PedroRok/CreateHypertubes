package com.pedrorok.hypertube.blocks;

import com.pedrorok.hypertube.blocks.blockentities.HyperEntranceBlockEntity;
import com.pedrorok.hypertube.managers.travel.TravelConstants;
import com.pedrorok.hypertube.registry.ModBlockEntities;
import com.pedrorok.hypertube.utils.MessageUtils;
import com.pedrorok.hypertube.utils.VoxelUtils;
import com.simibubi.create.content.kinetics.base.KineticBlock;
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
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
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Rok, Pedro Lucas nmm. Created on 21/04/2025
 * @project Create Hypertube
 */
public class HyperEntranceBlock extends KineticBlock implements EntityBlock, ICogWheel, TubeConnection {

    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
    public static final BooleanProperty LOCKED = BlockStateProperties.LOCKED;

    public static final BooleanProperty IN_FRONT = BooleanProperty.create("has_block_in_front");

    private static final VoxelShape SHAPE_NORTH = Block.box(0D, 0D, 0D, 16D, 16D, 23D);
    private static final VoxelShape SHAPE_SOUTH = Block.box(0D, 0D, -7D, 16D, 16D, 16D);
    private static final VoxelShape SHAPE_EAST = Block.box(-7D, 0D, 0D, 16D, 16D, 16D);
    private static final VoxelShape SHAPE_WEST = Block.box(0D, 0D, 0D, 23D, 16D, 16D);
    private static final VoxelShape SHAPE_UP = Block.box(0D, -7D, 0D, 16D, 16D, 16D);
    private static final VoxelShape SHAPE_DOWN = Block.box(0D, 0D, 0D, 16D, 23D, 16D);


    public HyperEntranceBlock(Properties properties) {
        super(properties);
        registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(OPEN, false)
                .setValue(LOCKED, false)
                .setValue(IN_FRONT, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, OPEN, IN_FRONT, LOCKED);
        super.createBlockStateDefinition(builder);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        if (player == null) {
            return this.defaultBlockState().setValue(FACING, context.getClickedFace().getOpposite())
                    .setValue(OPEN, false);
        }
        Direction direction = player.getDirection();
        if (player.getXRot() < -45) {
            direction = Direction.UP;
        } else if (player.getXRot() > 45) {
            direction = Direction.DOWN;
        }

        boolean isFrontBlocked = false;
        BlockPos relative = context.getClickedPos().relative(direction.getOpposite());
        if (!level.getBlockState(relative).getCollisionShape(level, relative).isEmpty()) {
            isFrontBlocked = true;
        }
        return this.defaultBlockState()
                .setValue(FACING, direction)
                .setValue(OPEN, false)
                .setValue(LOCKED, false)
                .setValue(IN_FRONT, isFrontBlocked);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block p_60512_, BlockPos p_60513_, boolean p_60514_) {
        super.neighborChanged(state, level, pos, p_60512_, p_60513_, p_60514_);
        updateInFrontProperty((Level) level, pos, state);
    }

    @Override
    public void onNeighborChange(BlockState state, LevelReader level, BlockPos pos, BlockPos neighbor) {
        super.onNeighborChange(state, level, pos, neighbor);
        updateInFrontProperty((Level) level, pos, state);
    }

    public void updateInFrontProperty(Level level, BlockPos pos, BlockState state) {
        boolean isFrontBlocked = false;
        Direction facing = state.getValue(FACING).getOpposite();
        BlockPos relative = pos.relative(facing);
        if (!level.getBlockState(relative).getCollisionShape(level, relative).isEmpty()) {
            isFrontBlocked = true;
        }
        level.setBlock(pos, state.setValue(IN_FRONT, isFrontBlocked), 3);
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
        return ModBlockEntities.HYPERTUBE_ENTRANCE.get().create(blockPos, blockState);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        return (level1, pos, state1, be) -> ((HyperEntranceBlockEntity) be).tick();
    }

    public boolean canTravelConnect(LevelAccessor world, BlockPos pos, Direction facing) {
        BlockState state = world.getBlockState(pos);
        return facing.getOpposite() == state.getValue(FACING)
               && state.getBlock() instanceof HyperEntranceBlock;
    }

    public VoxelShape getShape(BlockState state, @Nullable CollisionContext ctx) {
        if (ctx instanceof EntityCollisionContext ecc
            && ecc.getEntity() != null
            && ecc.getEntity().getPersistentData().getBoolean(TravelConstants.TRAVEL_TAG)) {
            return VoxelUtils.empty();
        }
        return switch (state.getValue(FACING)) {
            case SOUTH -> SHAPE_SOUTH;
            case EAST -> SHAPE_EAST;
            case WEST -> SHAPE_WEST;
            case UP -> SHAPE_UP;
            case DOWN -> SHAPE_DOWN;
            default -> SHAPE_NORTH;
        };
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

    @Override
    public boolean isSmallCog() {
        return true;
    }


    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        Player player = context.getPlayer();
        BlockState blockState = state.setValue(LOCKED, !state.getValue(LOCKED));
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        level.setBlock(pos, blockState, 3);

        if (blockState.getValue(LOCKED)) {
            MessageUtils.sendActionMessage(player,
                    Component.translatable("block.hypertube.hyper_entrance.manual_lock")
                            .append(" (")
                            .append(Component.translatable("block.hypertube.hyper_entrance.sneak_to_enter"))
                            .append(")")
                            .withStyle(ChatFormatting.GOLD), true);
        } else {
            MessageUtils.sendActionMessage(player,
                    Component.translatable("block.hypertube.hyper_entrance.automatic_lock")
                            .withStyle(ChatFormatting.GREEN), true);
        }
        level.playSound(player, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.75f, 1);

        return InteractionResult.SUCCESS;
    }


}
