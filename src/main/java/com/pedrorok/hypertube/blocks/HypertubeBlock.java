package com.pedrorok.hypertube.blocks;

import com.pedrorok.hypertube.blocks.blockentities.HypertubeBlockEntity;
import com.pedrorok.hypertube.items.HypertubeItem;
import com.pedrorok.hypertube.managers.TravelManager;
import com.pedrorok.hypertube.managers.placement.BezierConnection;
import com.pedrorok.hypertube.managers.placement.ResponseDTO;
import com.pedrorok.hypertube.managers.placement.SimpleConnection;
import com.pedrorok.hypertube.managers.placement.TubePlacement;
import com.pedrorok.hypertube.registry.ModBlockEntities;
import com.pedrorok.hypertube.registry.ModBlocks;
import com.pedrorok.hypertube.registry.ModDataComponent;
import com.pedrorok.hypertube.utils.MessageUtils;
import com.pedrorok.hypertube.utils.RayCastUtils;
import com.pedrorok.hypertube.utils.VoxelUtils;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author Rok, Pedro Lucas nmm. Created on 21/05/2025
 * @project Create Hypertube
 */
public class HypertubeBlock extends HypertubeBaseBlock implements TubeConnection, IBE<HypertubeBlockEntity> {

    public static final BooleanProperty NORTH_SOUTH = BooleanProperty.create("north_south");
    public static final BooleanProperty EAST_WEST = BooleanProperty.create("east_west");
    public static final BooleanProperty UP_DOWN = BooleanProperty.create("up_down");

    public static final VoxelShape SHAPE_NORTH_SOUTH = Block.box(0D, 0D, 4D, 16D, 16D, 11D);
    public static final VoxelShape SHAPE_EAST_WEST = Block.box(5D, 0D, 0D, 12D, 16D, 16D);
    public static final VoxelShape SHAPE_UP_DOWN = Block.box(0D, 4D, 0D, 16D, 11D, 16D);

    public HypertubeBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH_SOUTH, EAST_WEST, UP_DOWN);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(@NotNull BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (state == null) return null;
        for (Direction direction : Direction.values()) {
            BlockPos relative = context.getClickedPos().relative(direction);
            BlockState otherState = context.getLevel().getBlockState(relative);
            if (otherState.getBlock() instanceof TubeConnection) {
                return getState(Set.of(direction));
            }
        }
        return state.setValue(NORTH_SOUTH, false)
                .setValue(EAST_WEST, false)
                .setValue(UP_DOWN, false);
    }

    @Override
    public VoxelShape getShape(BlockState state, @Nullable CollisionContext ctx) {
        if (ctx instanceof EntityCollisionContext ecc
            && ecc.getEntity() != null
            && ecc.getEntity().getPersistentData().getBoolean(TravelManager.TRAVEL_TAG)) {
            return VoxelUtils.empty();
        }
        if (state.getValue(EAST_WEST)) {
            return SHAPE_EAST_WEST;
        }
        if (state.getValue(UP_DOWN)) {
            return SHAPE_UP_DOWN;
        }
        return SHAPE_NORTH_SOUTH;
    }

    @Override
    public void neighborChanged(@NotNull BlockState state, @NotNull Level world, @NotNull BlockPos pos, @NotNull Block block, @NotNull BlockPos pos1, boolean b) {
        super.neighborChanged(state, world, pos, block, pos1, b);
        BlockState newState = getStateFromBlockEntity(world, pos);
        world.setBlockAndUpdate(pos, newState);
    }


    private BlockState getStateFromBlockEntity(Level world, BlockPos pos) {
        BlockEntity be = world.getBlockEntity(pos);
        if (!(be instanceof HypertubeBlockEntity hypertube)) {
            return getState(world, pos);
        }

        BezierConnection connTo = hypertube.getConnectionTo();
        if (connTo != null) {
            Direction dirTo = connTo.getFromPos().direction();
            if (dirTo != null) {
                return getState(Set.of(dirTo));
            }
        }

        SimpleConnection connFrom = hypertube.getConnectionFrom();
        if (connFrom == null) {
            return getState(world, pos);
        }

        BlockEntity otherBE = world.getBlockEntity(connFrom.pos());
        if (!(otherBE instanceof HypertubeBlockEntity other)) {
            return getState(world, pos);
        }

        BezierConnection otherTo = other.getConnectionTo();
        if (otherTo != null) {
            Direction dirFrom = otherTo.getToPos().direction();
            if (dirFrom != null) {
                return getState(Set.of(dirFrom));
            }
        }
        return getState(world, pos);
    }


    public BlockState getState(Collection<Direction> activeDirections) {
        boolean northSouth = activeDirections.contains(Direction.NORTH) || activeDirections.contains(Direction.SOUTH);
        boolean eastWest = activeDirections.contains(Direction.EAST) || activeDirections.contains(Direction.WEST);
        boolean upDown = activeDirections.contains(Direction.UP) || activeDirections.contains(Direction.DOWN);
        // only one axis can be true at a time
        return defaultBlockState()
                .setValue(NORTH_SOUTH, northSouth)
                .setValue(EAST_WEST, eastWest && !northSouth)
                .setValue(UP_DOWN, upDown && !northSouth && !eastWest);
    }

    private BlockState getState(Level world, BlockPos pos) {
        boolean northSouth = isConnected(world, pos, Direction.NORTH) || isConnected(world, pos, Direction.SOUTH);
        boolean eastWest = isConnected(world, pos, Direction.EAST) || isConnected(world, pos, Direction.WEST);
        boolean upDown = isConnected(world, pos, Direction.UP) || isConnected(world, pos, Direction.DOWN);

        return defaultBlockState()
                .setValue(NORTH_SOUTH, northSouth)
                .setValue(EAST_WEST, eastWest && !northSouth)
                .setValue(UP_DOWN, upDown && !northSouth && !eastWest);
    }

    public void updateBlockStateFromEntity(Level world, BlockPos pos) {
        if (world.isClientSide()) return;

        BlockState newState = getStateFromBlockEntity(world, pos);
        updateBlockState(world, pos, newState);
    }

    public void updateBlockState(Level world, BlockPos pos, BlockState newState) {
        if (world.isClientSide()) return;

        BlockState currentState = world.getBlockState(pos);

        if (!currentState.equals(newState)) {
            world.setBlockAndUpdate(pos, newState);
        }
    }

    public List<Direction> getConnectedFaces(BlockState state) {
        List<Direction> directions = new ArrayList<>();
        if (state.getValue(NORTH_SOUTH)) {
            directions.add(Direction.NORTH);
            directions.add(Direction.SOUTH);
        }
        if (state.getValue(EAST_WEST)) {
            directions.add(Direction.EAST);
            directions.add(Direction.WEST);
        }
        if (state.getValue(UP_DOWN)) {
            directions.add(Direction.UP);
            directions.add(Direction.DOWN);
        }
        return directions;
    }

    public boolean isConnected(Level world, BlockPos pos, Direction facing) {
        return canConnect(world, pos, facing);
    }

    public boolean canConnect(LevelAccessor world, BlockPos pos, Direction facing) {
        return world.getBlockState(pos.relative(facing)).getBlock() instanceof TubeConnection;
    }

    @Override
    public boolean canTravelConnect(LevelAccessor world, BlockPos posSelf, Direction facing) {
        BlockPos relative = posSelf.relative(facing);
        BlockState otherState = world.getBlockState(relative);
        Block block = otherState.getBlock();
        return block instanceof TubeConnection
               && (!(block instanceof HypertubeBlock hypertubeBlock)
                   || canOtherConnectTo(world, relative, hypertubeBlock, facing));
    }

    private boolean canOtherConnectTo(LevelAccessor world, BlockPos otherPos, HypertubeBlock otherTube, Direction facing) {
        List<Direction> connectedFaces = otherTube.getConnectedFaces(otherTube.getState((Level) world, otherPos));
        return connectedFaces.isEmpty() || connectedFaces.contains(facing);
    }

    @Override
    public Class<HypertubeBlockEntity> getBlockEntityClass() {
        return HypertubeBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends HypertubeBlockEntity> getBlockEntityType() {
        return ModBlockEntities.HYPERTUBE.get();
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState state) {
        return ModBlockEntities.HYPERTUBE.get().create(blockPos, state);
    }

    @Override
    public @NotNull BlockState playerWillDestroy(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull Player player) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof HypertubeBlockEntity hypertubeEntity))
            return super.playerWillDestroy(level, pos, state, player);

        SimpleConnection connectionFrom = hypertubeEntity.getConnectionFrom();
        BezierConnection connectionTo = hypertubeEntity.getConnectionTo();

        if (connectionFrom != null) {
            BlockPos otherPos = connectionFrom.pos();
            BlockEntity otherBlock = level.getBlockEntity(otherPos);
            if (otherBlock instanceof HypertubeBlockEntity otherHypertubeEntity) {
                otherHypertubeEntity.setConnectionTo(null);
            }
        }

        if (connectionTo != null) {
            BlockPos otherPos = connectionTo.getToPos().pos();
            BlockEntity otherBlock = level.getBlockEntity(otherPos);
            if (otherBlock instanceof HypertubeBlockEntity otherHypertubeEntity) {
                otherHypertubeEntity.setConnectionFrom(null);
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!(placer instanceof Player player)) return;
        if (level.isClientSide()) return;

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof HypertubeBlockEntity hypertubeEntity)) return;
        if (!stack.hasFoil()) return;

        SimpleConnection connectionFrom = stack.get(ModDataComponent.TUBE_CONNECTING_FROM);
        if (connectionFrom == null) return;

        Direction finalDirection = RayCastUtils.getDirectionFromHitResult(player, null, true);
        SimpleConnection connectionTo = new SimpleConnection(pos, finalDirection);
        BezierConnection bezierConnection = BezierConnection.of(connectionFrom, connectionTo);

        HypertubeItem.clearConnection(stack);

        ResponseDTO validation = bezierConnection.getValidation();
        if (validation.valid()) {
            validation = TubePlacement.checkSurvivalItems(player, (int) bezierConnection.distance(), true);
        }

        if (!validation.valid()) {
            MessageUtils.sendActionMessage(player, "§c" + validation.errorMessage());
            return;
        }
        TubePlacement.checkSurvivalItems(player, (int) bezierConnection.distance(), false);


        BlockEntity otherBlockEntity = level.getBlockEntity(connectionFrom.pos());
        boolean inverted = false;
        if (otherBlockEntity instanceof HypertubeBlockEntity otherHypertubeEntity) {
            if (otherHypertubeEntity.getConnectionTo() == null) {
                otherHypertubeEntity.setConnectionTo(bezierConnection);
            } else if (otherHypertubeEntity.getConnectionFrom() == null) {
                bezierConnection = bezierConnection.invert();
                connectionTo = bezierConnection.getFromPos();
                otherHypertubeEntity.setConnectionFrom(connectionTo);
                inverted = true;
            } else {
                player.displayClientMessage(Component.literal("Invalid connection"), true);
                return;
            }
        }


        if (inverted)
            hypertubeEntity.setConnectionTo(bezierConnection);
        else
            hypertubeEntity.setConnectionFrom(connectionFrom);


        if (!level.isClientSide()
            && level.getBlockState(pos).getBlock() instanceof HypertubeBlock hypertubeBlock) {
            hypertubeBlock.updateBlockState(level, pos, hypertubeBlock.getState(List.of(finalDirection)));
        }
    }

    @Override
    public @NotNull ItemStack getCloneItemStack(@NotNull LevelReader level, @NotNull BlockPos pos, @NotNull BlockState state) {
        return ModBlocks.HYPERTUBE.asStack();
    }
}