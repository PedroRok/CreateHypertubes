package com.pedrorok.hypertube.blocks;

import com.pedrorok.hypertube.blocks.blockentities.HypertubeBlockEntity;
import com.pedrorok.hypertube.items.HypertubeItem;
import com.pedrorok.hypertube.managers.TravelManager;
import com.pedrorok.hypertube.managers.placement.BezierConnection;
import com.pedrorok.hypertube.managers.placement.SimpleConnection;
import com.pedrorok.hypertube.registry.ModBlockEntities;
import com.pedrorok.hypertube.registry.ModDataComponent;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class HypertubeBlock extends HypertubeBaseBlock implements TubeConnection, IBE<HypertubeBlockEntity> {

    public static final BooleanProperty DOWN = BooleanProperty.create("down");
    public static final BooleanProperty UP = BooleanProperty.create("up");
    public static final BooleanProperty NORTH = BooleanProperty.create("north");
    public static final BooleanProperty SOUTH = BooleanProperty.create("south");
    public static final BooleanProperty WEST = BooleanProperty.create("west");
    public static final BooleanProperty EAST = BooleanProperty.create("east");

    public static final VoxelShape SHAPE_NORTH = Block.box(2D, 2D, 0D, 14D, 14D, 2D);
    public static final VoxelShape SHAPE_SOUTH = Block.box(2D, 2D, 14D, 14D, 14D, 16D);
    public static final VoxelShape SHAPE_EAST = Block.box(14D, 2D, 2D, 16D, 14D, 14D);
    public static final VoxelShape SHAPE_WEST = Block.box(0D, 2D, 2D, 2D, 14D, 14D);
    public static final VoxelShape SHAPE_UP = Block.box(2D, 14D, 2D, 14D, 16D, 14D);
    public static final VoxelShape SHAPE_DOWN = Block.box(2D, 0D, 2D, 14D, 2D, 14D);
    public static final VoxelShape SHAPE_CORE = Block.box(2D, 2D, 2D, 14D, 14D, 14D);

    public HypertubeBlock() {
        super(PROPERTIES);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(DOWN, UP, NORTH, SOUTH, WEST, EAST);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(@NotNull BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (state == null) return null;
        return state.setValue(DOWN, false)
                .setValue(UP, false)
                .setValue(NORTH, false)
                .setValue(SOUTH, false)
                .setValue(WEST, false)
                .setValue(EAST, false);
    }

    @Override
    public VoxelShape getShape(BlockState state, @Nullable CollisionContext ctx) {
        if (ctx instanceof EntityCollisionContext ecc
            && ecc.getEntity() != null
            && ecc.getEntity().getPersistentData().getBoolean(TravelManager.TRAVEL_TAG)) {
            return VoxelUtils.empty();
        }

        VoxelShape shape = SHAPE_CORE;
        if (state.getValue(UP)) shape = VoxelUtils.combine(shape, SHAPE_UP);
        if (state.getValue(DOWN)) shape = VoxelUtils.combine(shape, SHAPE_DOWN);
        if (state.getValue(SOUTH)) shape = VoxelUtils.combine(shape, SHAPE_SOUTH);
        if (state.getValue(NORTH)) shape = VoxelUtils.combine(shape, SHAPE_NORTH);
        if (state.getValue(EAST)) shape = VoxelUtils.combine(shape, SHAPE_EAST);
        if (state.getValue(WEST)) shape = VoxelUtils.combine(shape, SHAPE_WEST);
        return shape;
    }

    @Override
    public void neighborChanged(@NotNull BlockState state, @NotNull Level world, @NotNull BlockPos pos, @NotNull Block block, @NotNull BlockPos pos1, boolean b) {
        super.neighborChanged(state, world, pos, block, pos1, b);
        BlockState newState = getState(world, pos);
        if (!state.getProperties().stream().allMatch(p -> state.getValue(p).equals(newState.getValue(p)))) {
            world.setBlockAndUpdate(pos, newState);
        }
    }

    private BlockState getState(Level world, BlockPos pos) {
        Set<BlockPos> visited = new HashSet<>();
        List<Direction> locked = findDominantDirection(world, pos, visited);
        return defaultBlockState()
                .setValue(UP, locked.contains(Direction.UP) && isConnected(world, pos, Direction.UP))
                .setValue(DOWN, locked.contains(Direction.DOWN) && isConnected(world, pos, Direction.DOWN))
                .setValue(NORTH,locked.contains(Direction.NORTH) && isConnected(world, pos, Direction.NORTH))
                .setValue(SOUTH,locked.contains(Direction.SOUTH) && isConnected(world, pos, Direction.SOUTH))
                .setValue(EAST, locked.contains(Direction.EAST) && isConnected(world, pos, Direction.EAST))
                .setValue(WEST, locked.contains(Direction.WEST) && isConnected(world, pos, Direction.WEST));
    }

    private List<Direction> findDominantDirection(Level world, BlockPos pos, Set<BlockPos> visited) {
        Queue<BlockPos> queue = new ArrayDeque<>();
        Map<Direction, Integer> directionCount = new EnumMap<>(Direction.class);
        queue.add(pos);
        visited.add(pos);

        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            for (Direction direction : Direction.values()) {
                BlockPos adjacent = current.relative(direction);
                if (visited.contains(adjacent)) continue;
                if (!(world.getBlockState(adjacent).getBlock() instanceof TubeConnection)) continue;
                visited.add(adjacent);
                queue.add(adjacent);
                directionCount.put(direction, directionCount.getOrDefault(direction, 0) + 1);
            }
        }

        Direction direction = directionCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(Direction.NORTH);
        return List.of(direction, direction.getOpposite());
    }

    public List<Direction> getConnectedFaces(BlockState state) {
        List<Direction> directions = new ArrayList<>();
        if (state.getValue(UP)) directions.add(Direction.UP);
        if (state.getValue(DOWN)) directions.add(Direction.DOWN);
        if (state.getValue(NORTH)) directions.add(Direction.NORTH);
        if (state.getValue(SOUTH)) directions.add(Direction.SOUTH);
        if (state.getValue(EAST)) directions.add(Direction.EAST);
        if (state.getValue(WEST)) directions.add(Direction.WEST);
        return directions;
    }

    public boolean isConnected(Level world, BlockPos pos, Direction facing) {
        return canConnect(world, pos, facing);
    }

    public boolean canConnect(LevelAccessor world, BlockPos pos, Direction facing) {
        return world.getBlockState(pos.relative(facing)).getBlock() instanceof TubeConnection;
    }

    @Override
    public Class<HypertubeBlockEntity> getBlockEntityClass() {
        return HypertubeBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends HypertubeBlockEntity> getBlockEntityType() {
        return ModBlockEntities.HYPERTUBE_ENTITY.get();
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState state) {
        return ModBlockEntities.HYPERTUBE_ENTITY.get().create(blockPos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == ModBlockEntities.HYPERTUBE_ENTITY.get() ? HypertubeBlockEntity::tick : null;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean update) {
        super.onRemove(state, level, pos, newState, update);
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
        if (!bezierConnection.isValid()) {
            player.displayClientMessage(Component.literal("Invalid connection"), true);
            return;
        }

        BlockEntity otherBlockEntity = level.getBlockEntity(connectionFrom.pos());
        if (otherBlockEntity instanceof HypertubeBlockEntity otherHypertubeEntity) {
            otherHypertubeEntity.setConnectionTo(bezierConnection);
        }

        hypertubeEntity.setConnectionFrom(connectionFrom);
    }


}
