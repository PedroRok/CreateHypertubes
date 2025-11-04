package com.pedrorok.hypertube.blocks;

import com.pedrorok.hypertube.blocks.blockentities.HypertubeBlockEntity;
import com.pedrorok.hypertube.core.connection.BezierConnection;
import com.pedrorok.hypertube.core.connection.SimpleConnection;
import com.pedrorok.hypertube.core.connection.interfaces.IConnection;
import com.pedrorok.hypertube.core.connection.interfaces.ITubeConnection;
import com.pedrorok.hypertube.core.connection.interfaces.ITubeConnectionEntity;
import com.pedrorok.hypertube.core.travel.TravelConstants;
import com.pedrorok.hypertube.registry.ModBlockEntities;
import com.pedrorok.hypertube.registry.ModBlocks;
import com.pedrorok.hypertube.registry.ModDataComponent;
import com.pedrorok.hypertube.registry.ModItems;
import com.pedrorok.hypertube.utils.MessageUtils;
import com.pedrorok.hypertube.utils.RayCastUtils;
import com.pedrorok.hypertube.utils.TubeUtils;
import com.pedrorok.hypertube.utils.VoxelUtils;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
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
public class HypertubeBlock extends TubeBlock implements EntityBlock {

    public static final BooleanProperty CONNECTED = BooleanProperty.create("connected");
    public static final BooleanProperty NORTH_SOUTH = BooleanProperty.create("north_south");
    public static final BooleanProperty EAST_WEST = BooleanProperty.create("east_west");
    public static final BooleanProperty UP_DOWN = BooleanProperty.create("up_down");

    public static final VoxelShape SHAPE_NORTH_SOUTH = Block.box(0D, 0D, 4D, 16D, 16D, 11D);
    public static final VoxelShape SHAPE_EAST_WEST = Block.box(5D, 0D, 0D, 12D, 16D, 16D);
    public static final VoxelShape SHAPE_UP_DOWN = Block.box(0D, 4D, 0D, 16D, 11D, 16D);

    public HypertubeBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(this.stateDefinition.any()
                .setValue(CONNECTED, false)
                .setValue(NORTH_SOUTH, false)
                .setValue(WATERLOGGED, false)
                .setValue(EAST_WEST, false)
                .setValue(UP_DOWN, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH_SOUTH, EAST_WEST, UP_DOWN, CONNECTED, WATERLOGGED);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(@NotNull BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (state == null) return null;
        for (Direction direction : Direction.values()) {
            BlockPos relative = context.getClickedPos().relative(direction);
            BlockEntity otherEntity = context.getLevel().getBlockEntity(relative);
            if (otherEntity instanceof ITubeConnectionEntity otherTube
                && otherTube.getFacesConnectable().contains(direction.getOpposite())) {
                return getState(state, List.of(direction), true);
            }
        }

        FluidState fluidstate = context.getLevel().getFluidState(context.getClickedPos());
        if (context.getPlayer() == null) {
            return state.setValue(NORTH_SOUTH, false)
                    .setValue(EAST_WEST, false)
                    .setValue(UP_DOWN, false)
                    .setValue(CONNECTED, false)
                    .setValue(WATERLOGGED, fluidstate.is(Fluids.WATER));
        }

        Player player = context.getPlayer();
        Direction direction = context.getPlayer().getDirection();
        if (player.getXRot() < -45) {
            direction = Direction.UP;
        } else if (player.getXRot() > 45) {
            direction = Direction.DOWN;
        }

        return getState(state, List.of(direction), false).setValue(WATERLOGGED, fluidstate.is(Fluids.WATER));
    }

    // ------- Collision Shapes -------
    @Override
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
    public Item getItem() {
        return ModItems.HYPERTUBE.get();
    }

    @Override
    public void neighborChanged(@NotNull BlockState state, @NotNull Level world, @NotNull BlockPos pos,
                                @NotNull Block block, @NotNull BlockPos neighborPos, boolean isMoving) {
        super.neighborChanged(state, world, pos, block, neighborPos, isMoving);

        if (world.isClientSide()) return;

        BlockState newState = getStateFromBlockEntity(state, world, pos);
        if (!state.equals(newState)) {
            world.setBlock(pos, newState, Block.UPDATE_ALL);

            // Sincroniza BlockEntity após mudança de vizinho
            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof HypertubeBlockEntity hypertube) {
                hypertube.setChanged();
                world.sendBlockUpdated(pos, state, newState, Block.UPDATE_CLIENTS);
            }
        }
    }

    private BlockState getStateFromBlockEntity(BlockState blockState, Level world, BlockPos pos) {
        BlockEntity be = world.getBlockEntity(pos);
        if (!(be instanceof HypertubeBlockEntity hypertube)) {
            return getState(blockState, world, pos);
        }
        IConnection conn = hypertube.getConnectionOne();
        SimpleConnection connection = IConnection.getSameConnectionBlockPos(conn, world, pos);
        if (connection != null) {
            return getState(blockState, Set.of(connection.direction()), true);
        }

        conn = hypertube.getConnectionTwo();
        connection = IConnection.getSameConnectionBlockPos(conn, world, pos);
        if (connection != null) {
            return getState(blockState, Set.of(connection.direction()), true);
        }

        return getState(blockState, world, pos);
    }

    public BlockState getState(BlockState blockState, Collection<Direction> activeDirections, boolean connected) {
        if (activeDirections == null) {
            return blockState
                    .setValue(NORTH_SOUTH, false)
                    .setValue(EAST_WEST, false)
                    .setValue(UP_DOWN, false);
        }
        boolean northSouth = activeDirections.contains(Direction.NORTH) || activeDirections.contains(Direction.SOUTH);
        boolean eastWest = activeDirections.contains(Direction.EAST) || activeDirections.contains(Direction.WEST);
        boolean upDown = activeDirections.contains(Direction.UP) || activeDirections.contains(Direction.DOWN);
        // only one axis can be true at a time
        return blockState
                .setValue(NORTH_SOUTH, northSouth)
                .setValue(EAST_WEST, eastWest && !northSouth)
                .setValue(UP_DOWN, upDown && !northSouth && !eastWest)
                .setValue(CONNECTED, connected && (northSouth || eastWest || upDown));
    }

    private BlockState getState(BlockState blockState, Level world, BlockPos pos) {
        if (blockState == null) {
            blockState = defaultBlockState();
        }
        boolean northSouth = isConnected(world, pos, Direction.NORTH) || isConnected(world, pos, Direction.SOUTH);
        boolean eastWest = isConnected(world, pos, Direction.EAST) || isConnected(world, pos, Direction.WEST);
        boolean upDown = isConnected(world, pos, Direction.UP) || isConnected(world, pos, Direction.DOWN);

        return blockState
                .setValue(NORTH_SOUTH, northSouth)
                .setValue(EAST_WEST, eastWest && !northSouth)
                .setValue(UP_DOWN, upDown && !northSouth && !eastWest)
                .setValue(CONNECTED, northSouth || eastWest || upDown);
    }

    public void updateBlockStateFromEntity(BlockState state, Level world, BlockPos pos) {
        if (world.isClientSide()) return;

        BlockState newState = getStateFromBlockEntity(state, world, pos);
        updateBlockState(world, pos, newState);

        // CRÍTICO: Força sincronização da BlockEntity após atualizar o estado
        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof HypertubeBlockEntity hypertube) {
            hypertube.setChanged();
            world.sendBlockUpdated(pos, state, newState, Block.UPDATE_CLIENTS);
        }
    }

    public void updateBlockState(Level world, BlockPos pos, BlockState newState) {
        if (world.isClientSide()) return;

        BlockState currentState = world.getBlockState(pos);

        if (!currentState.equals(newState)) {
            // Usa flags corretas para sincronização:
            // Block.UPDATE_ALL = 3 (UPDATE_NEIGHBORS | UPDATE_CLIENTS)
            world.setBlock(pos, newState, Block.UPDATE_ALL);

            // Notifica vizinhos sobre a mudança
            world.updateNeighborsAt(pos, newState.getBlock());

            // Força sincronização da BlockEntity
            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof HypertubeBlockEntity hypertube) {
                hypertube.setChanged();
                // Envia pacote de atualização para clientes
                world.sendBlockUpdated(pos, currentState, newState, Block.UPDATE_CLIENTS);
            }
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

    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState state) {
        return ModBlockEntities.HYPERTUBE.get().create(blockPos, state);
    }

    @Override
    public void setPlacedBy(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state,
                            @Nullable LivingEntity placer, @NotNull ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!(placer instanceof Player player)) return;
        if (level.isClientSide()) return;

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof ITubeConnectionEntity thisConnection)) return;
        if (!stack.hasFoil()) {
            level.playSound(null, pos, getSoundType(state, level, pos, placer).getPlaceSound(), SoundSource.BLOCKS,
                    1, level.random.nextFloat() * 0.1f + 0.9f);
            return;
        }

        SimpleConnection connectionFrom = stack.get(ModDataComponent.TUBE_CONNECTING_FROM);
        if (connectionFrom == null) return;

        Direction finalDirection = RayCastUtils.getDirectionFromHitResult(player,
                () -> state.getBlock() instanceof ITubeConnection, true);
        SimpleConnection connectionTo = new SimpleConnection(pos, finalDirection);
        BezierConnection bezierConnection = BezierConnection.of(connectionFrom, connectionTo);

        if (!TubeUtils.checkPlayerPlacingBlockValidation(player, bezierConnection, level)) {
            level.playSound(placer, pos, SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.BLOCKS, 1, 0.5f);
            return;
        }

        BlockEntity otherBlockEntity = level.getBlockEntity(connectionFrom.pos());
        if (!(otherBlockEntity instanceof ITubeConnectionEntity otherConnection)) return;

        level.playSound(null, pos, getSoundType(state, level, pos, placer).getPlaceSound(), SoundSource.BLOCKS,
                1, level.random.nextFloat() * 0.1f + 0.9f);

        if (!otherConnection.hasConnectionAvailable()) {
            MessageUtils.sendActionMessage(player,
                    Component.translatable("placement.create_hypertube.invalid_conn").withColor(0xFF0000), true);
            return;
        }

        // Define conexões
        otherConnection.setConnection(bezierConnection, bezierConnection.getFromPos().direction());
        thisConnection.setConnection(connectionFrom, finalDirection);

        // CRÍTICO: Força sincronização após criar conexões
        if (blockEntity instanceof HypertubeBlockEntity hypertube) {
            hypertube.setChanged();
            BlockState newState = getState(state, List.of(finalDirection), true);
            level.setBlock(pos, newState, Block.UPDATE_ALL);
            level.sendBlockUpdated(pos, state, newState, Block.UPDATE_CLIENTS);
        }

        // Também sincroniza o outro bloco
        if (otherBlockEntity instanceof HypertubeBlockEntity otherHypertube) {
            otherHypertube.setChanged();
            BlockState otherState = level.getBlockState(connectionFrom.pos());
            level.sendBlockUpdated(connectionFrom.pos(), otherState, otherState, Block.UPDATE_CLIENTS);
        }

        MessageUtils.sendActionMessage(player, Component.empty(), true);
    }

    @Override
    public boolean propagatesSkylightDown(@NotNull BlockState state, @NotNull BlockGetter reader, @NotNull BlockPos pos) {
        return true;
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        if (context.getPlayer() == null) return InteractionResult.PASS;
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();

        if (state.getValue(CONNECTED)) {
            if (level.getBlockEntity(pos) instanceof ITubeConnectionEntity tube) {
                tube.wrenchClicked(context.getClickedFace());
            }
            updateAfterWrenched(state, context);

            // CRÍTICO: Sincroniza após usar wrench em conexão
            if (!level.isClientSide() && level.getBlockEntity(pos) instanceof HypertubeBlockEntity hypertube) {
                hypertube.setChanged();
                BlockState newState = level.getBlockState(pos);
                level.sendBlockUpdated(pos, state, newState, Block.UPDATE_CLIENTS);
            }

            IWrenchable.playRotateSound(context.getLevel(), context.getClickedPos());
            return InteractionResult.SUCCESS;
        }

        // Rotação sem conexão
        if (state.getValue(EAST_WEST)) {
            state = state.setValue(EAST_WEST, false).setValue(UP_DOWN, true);
        } else if (state.getValue(UP_DOWN)) {
            state = state.setValue(UP_DOWN, false).setValue(NORTH_SOUTH, true);
        } else if (state.getValue(NORTH_SOUTH)) {
            state = state.setValue(NORTH_SOUTH, false).setValue(EAST_WEST, true);
        } else {
            state = getState(state, List.of(context.getClickedFace()), false);
        }

        level.playSound(player, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.75f, 1);

        return super.onWrenched(state, context);
    }
}