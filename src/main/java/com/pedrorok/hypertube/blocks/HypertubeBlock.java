package com.pedrorok.hypertube.blocks;

import com.pedrorok.hypertube.blocks.blockentities.HypertubeBlockEntity;
import com.pedrorok.hypertube.core.connection.*;
import com.pedrorok.hypertube.core.connection.interfaces.IConnection;
import com.pedrorok.hypertube.core.connection.interfaces.TubeConnection;
import com.pedrorok.hypertube.core.connection.interfaces.TubeConnectionEntity;
import com.pedrorok.hypertube.core.travel.TravelConstants;
import com.pedrorok.hypertube.registry.ModBlockEntities;
import com.pedrorok.hypertube.registry.ModBlocks;
import com.pedrorok.hypertube.registry.ModDataComponent;
import com.pedrorok.hypertube.utils.MessageUtils;
import com.pedrorok.hypertube.utils.RayCastUtils;
import com.pedrorok.hypertube.utils.TubeUtils;
import com.pedrorok.hypertube.utils.VoxelUtils;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.WaterloggedTransparentBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent;
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
public class HypertubeBlock extends WaterloggedTransparentBlock implements TubeConnection, IBE<HypertubeBlockEntity>, IWrenchable {

    public static final BooleanProperty CONNECTED = BooleanProperty.create("connected");
    public static final BooleanProperty NORTH_SOUTH = BooleanProperty.create("north_south");
    public static final BooleanProperty EAST_WEST = BooleanProperty.create("east_west");
    public static final BooleanProperty UP_DOWN = BooleanProperty.create("up_down");

    public static final IntegerProperty TUBE_SEGMENTS = IntegerProperty.create("tube_segments", 1, 4);

    public static final VoxelShape SHAPE_NORTH_SOUTH = Block.box(0D, 0D, 4D, 16D, 16D, 11D);
    public static final VoxelShape SHAPE_EAST_WEST = Block.box(5D, 0D, 0D, 12D, 16D, 16D);
    public static final VoxelShape SHAPE_UP_DOWN = Block.box(0D, 4D, 0D, 16D, 11D, 16D);

    public HypertubeBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH_SOUTH, EAST_WEST, UP_DOWN, CONNECTED, TUBE_SEGMENTS, WATERLOGGED);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(@NotNull BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (state == null) return null;
        for (Direction direction : Direction.values()) {
            BlockPos relative = context.getClickedPos().relative(direction);
            BlockState otherState = context.getLevel().getBlockState(relative);
            if (otherState.getBlock() instanceof TubeConnection) {
                return getState(List.of(direction), true);
            }
        }

        if (context.getPlayer() == null) {
            return state.setValue(NORTH_SOUTH, false)
                    .setValue(EAST_WEST, false)
                    .setValue(UP_DOWN, false)
                    .setValue(CONNECTED, false);
        }

        Player player = context.getPlayer();
        Direction direction = context.getPlayer().getDirection();
        if (player.getXRot() < -45) {
            direction = Direction.UP;
        } else if (player.getXRot() > 45) {
            direction = Direction.DOWN;
        }

        return getState(List.of(direction), false);
    }

    public VoxelShape getShape(BlockState state, @Nullable CollisionContext ctx) {
        if (ctx instanceof EntityCollisionContext ecc
            && ecc.getEntity() != null
            && ecc.getEntity().getPersistentData().getBoolean(TravelConstants.TRAVEL_TAG)) {
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

        // TODO: Refactor

        IConnection connTo = hypertube.getConnectionOne();
        if (connTo != null) {
            Direction dirTo = connTo.getThisEntranceDirection(world);
            if (dirTo != null) {
                return getState(Set.of(dirTo), true);
            }
        }

        IConnection connFrom = hypertube.getConnectionTwo();
        if (connFrom == null) {
            return getState(world, pos);
        }

        BlockEntity otherBE = world.getBlockEntity(connFrom.getThisConnection().pos());
        if (!(otherBE instanceof HypertubeBlockEntity other)) {
            return getState(world, pos);
        }
        return getState(world, pos);
    }


    public BlockState getState(Collection<Direction> activeDirections, boolean connected) {
        if (activeDirections == null) {
            return defaultBlockState()
                    .setValue(NORTH_SOUTH, false)
                    .setValue(EAST_WEST, false)
                    .setValue(UP_DOWN, false);
        }
        boolean northSouth = activeDirections.contains(Direction.NORTH) || activeDirections.contains(Direction.SOUTH);
        boolean eastWest = activeDirections.contains(Direction.EAST) || activeDirections.contains(Direction.WEST);
        boolean upDown = activeDirections.contains(Direction.UP) || activeDirections.contains(Direction.DOWN);
        // only one axis can be true at a time
        return defaultBlockState()
                .setValue(NORTH_SOUTH, northSouth)
                .setValue(EAST_WEST, eastWest && !northSouth)
                .setValue(UP_DOWN, upDown && !northSouth && !eastWest)
                .setValue(CONNECTED, connected && (northSouth || eastWest || upDown));
    }

    private BlockState getState(Level world, BlockPos pos) {
        boolean northSouth = isConnected(world, pos, Direction.NORTH) || isConnected(world, pos, Direction.SOUTH);
        boolean eastWest = isConnected(world, pos, Direction.EAST) || isConnected(world, pos, Direction.WEST);
        boolean upDown = isConnected(world, pos, Direction.UP) || isConnected(world, pos, Direction.DOWN);

        return defaultBlockState()
                .setValue(NORTH_SOUTH, northSouth)
                .setValue(EAST_WEST, eastWest && !northSouth)
                .setValue(UP_DOWN, upDown && !northSouth && !eastWest)
                .setValue(CONNECTED, northSouth || eastWest || upDown);
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
        if (!state.getValue(CONNECTED)) {
            return List.of();
        }
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
        return playerWillDestroy(level, pos, state, player, false);
    }

    private BlockState playerWillDestroy(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull Player player, boolean wrenched) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof TubeConnectionEntity tube))
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
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!(placer instanceof Player player)) return;
        if (level.isClientSide()) return;

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof TubeConnectionEntity thisConnection)) return;
        if (!stack.hasFoil()) {
            level.playSound(null, pos, getSoundType(state, level, pos, placer).getPlaceSound(), SoundSource.BLOCKS,
                    1, level.random.nextFloat() * 0.1f + 0.9f);
            return;
        }

        SimpleConnection connectionFrom = stack.get(ModDataComponent.TUBE_CONNECTING_FROM);
        if (connectionFrom == null) return;

        Direction finalDirection = RayCastUtils.getDirectionFromHitResult(player, null, true);
        SimpleConnection connectionTo = new SimpleConnection(pos, finalDirection);
        BezierConnection bezierConnection = BezierConnection.of(connectionFrom, connectionTo);

        if (!TubeUtils.checkPlayerPlacingBlockValidation(player, bezierConnection, level)) {
            level.playSound(placer, pos, SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.BLOCKS,
                    1, 0.5f);
            return;
        }
        BlockEntity otherBlockEntity = level.getBlockEntity(connectionFrom.pos());
        if (!(otherBlockEntity instanceof TubeConnectionEntity otherConnection)) return;

        level.playSound(null, pos, getSoundType(state, level, pos, placer).getPlaceSound(), SoundSource.BLOCKS,
                1, level.random.nextFloat() * 0.1f + 0.9f);

        if (!otherConnection.hasConnectionAvailable()) {
            MessageUtils.sendActionMessage(player, Component.translatable("placement.create_hypertube.invalid_conn").withColor(0xFF0000), true);
            return;
        }

        otherConnection.setConnection(bezierConnection, bezierConnection.getFromPos().direction());
        thisConnection.setConnection(connectionFrom, finalDirection);

        MessageUtils.sendActionMessage(player, Component.empty(), true);
        if (!(level.getBlockState(pos).getBlock() instanceof HypertubeBlock hypertubeBlock)) return;
        hypertubeBlock.updateBlockState(level, pos, hypertubeBlock.getState(List.of(finalDirection), true));
    }

    @Override
    public @NotNull ItemStack getCloneItemStack(@NotNull LevelReader level, @NotNull BlockPos pos, @NotNull BlockState state) {
        return ModBlocks.HYPERTUBE.asStack();
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos, Player player) {
        return ModBlocks.HYPERTUBE.asStack();
    }


    @Override
    public boolean propagatesSkylightDown(@NotNull BlockState state, @NotNull BlockGetter reader, @NotNull BlockPos pos) {
        return true;
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
    public InteractionResult onSneakWrenched(BlockState state, UseOnContext context) {
        if (context.getPlayer() == null) return InteractionResult.PASS;
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();

        playerWillDestroy(world, context.getClickedPos(), state, context.getPlayer(), true);

        if (!(world instanceof ServerLevel))
            return InteractionResult.SUCCESS;

        BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(world, pos, world.getBlockState(pos), player);
        NeoForge.EVENT_BUS.post(event);
        if (event.isCanceled())
            return InteractionResult.SUCCESS;

        world.destroyBlock(pos, false);
        IWrenchable.playRemoveSound(world, pos);
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        if (context.getPlayer() == null) return InteractionResult.PASS;
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        level.playSound(player, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.75f, 1);
        if (state.getValue(CONNECTED)) {

            state = state.setValue(TUBE_SEGMENTS, state.getValue(TUBE_SEGMENTS) == 1 ? 2 : 1);
            updateAfterWrenched(state, context);

            return IWrenchable.super.onWrenched(state, context);
        }
        if (state.getValue(EAST_WEST)) {
            state = state.setValue(EAST_WEST, false)
                    .setValue(UP_DOWN, true);
        } else if (state.getValue(UP_DOWN)) {
            state = state.setValue(UP_DOWN, false)
                    .setValue(NORTH_SOUTH, true);
        } else if (state.getValue(NORTH_SOUTH)) {
            state = state.setValue(NORTH_SOUTH, false)
                    .setValue(EAST_WEST, true);
        } else {
            state = getState(List.of(context.getClickedFace()), false);
        }

        return IWrenchable.super.onWrenched(state, context);
    }
}