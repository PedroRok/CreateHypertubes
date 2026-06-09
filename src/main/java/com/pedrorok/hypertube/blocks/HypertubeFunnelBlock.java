package com.pedrorok.hypertube.blocks;

import com.pedrorok.hypertube.blocks.blockentities.HypertubeFunnelBlockEntity;
import com.pedrorok.hypertube.core.connection.interfaces.ITubeConnection;
import com.pedrorok.hypertube.core.travel.TravelConstants;
import com.pedrorok.hypertube.registry.ModBlockEntities;
import com.pedrorok.hypertube.registry.ModBlocks;
import com.pedrorok.hypertube.utils.VoxelUtils;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
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

import java.util.List;

public class HypertubeFunnelBlock extends ActionTubeBlock implements EntityBlock, ITubeConnection, ICogWheel {

    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    private static final VoxelShape SHAPE_NORTH = Block.box(0D, 0D, 0D, 16D, 16D, 16D);
    private static final VoxelShape SHAPE_SOUTH = Block.box(0D, 0D, 0D, 16D, 16D, 16D);
    private static final VoxelShape SHAPE_EAST = Block.box(0D, 0D, 0D, 16D, 16D, 16D);
    private static final VoxelShape SHAPE_WEST = Block.box(0D, 0D, 0D, 16D, 16D, 16D);
    private static final VoxelShape SHAPE_UP = Block.box(0D, 0D, 0D, 16D, 16D, 16D);
    private static final VoxelShape SHAPE_DOWN = Block.box(0D, 0D, 0D, 16D, 16D, 16D);

    public HypertubeFunnelBlock(Properties properties) {
        super(properties);
        registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(POWERED, false)
                .setValue(WATERLOGGED, false)
                .setValue(POWER, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED, WATERLOGGED, POWER);
        super.createBlockStateDefinition(builder);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Player player = context.getPlayer();
        Direction direction = context.getClickedFace().getOpposite();
        if (player != null) {
            direction = player.getDirection();
            if (player.getXRot() < -45) {
                direction = Direction.UP;
            } else if (player.getXRot() > 45) {
                direction = Direction.DOWN;
            }
        }
        return this.defaultBlockState()
                .setValue(FACING, direction)
                .setValue(POWERED, false)
                .setValue(WATERLOGGED, false);
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
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(FACING).getAxis();
    }

    @Override
    public Item getItem() {
        return ModBlocks.HYPERTUBE_FUNNEL.asItem();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
        return ModBlockEntities.HYPERTUBE_FUNNEL.get().create(blockPos, blockState);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        return (level1, pos, state1, be) -> ((HypertubeFunnelBlockEntity) be).tick();
    }

    @Override
    public List<Direction> getConnectedFaces(BlockState state) {
        return List.of(state.getValue(FACING).getOpposite());
    }

    public boolean canTravelConnect(LevelAccessor world, BlockPos pos, Direction facing) {
        BlockState state = world.getBlockState(pos);
        return facing.getOpposite() == state.getValue(FACING)
               && state.getBlock() instanceof HypertubeFunnelBlock;
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        if (context.getLevel().isClientSide) return InteractionResult.SUCCESS;
        if (super.onWrenched(state, context) == InteractionResult.SUCCESS) return InteractionResult.SUCCESS;
        return InteractionResult.PASS;
    }

    @Override
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
    public boolean isSmallCog() {
        return true;
    }

    @Override
    protected BooleanProperty propertyToUpdate() {
        return POWERED;
    }
}
