package com.pedrorok.hypertube.blocks;

import com.pedrorok.hypertube.blocks.blockentities.parent.ActionTubeBlockEntity;
import com.pedrorok.hypertube.blocks.blockentities.parent.TravelInteractTubeBlockEntity;
import com.pedrorok.hypertube.core.connection.interfaces.ITubeActionPoint;
import com.pedrorok.hypertube.core.connection.interfaces.ITubeConnectionEntity;
import com.pedrorok.hypertube.core.travel.TravelConstants;
import com.pedrorok.hypertube.core.travel.TravelPathMover;
import com.pedrorok.hypertube.registry.ModBlockEntities;
import com.pedrorok.hypertube.registry.ModBlocks;
import com.pedrorok.hypertube.utils.VoxelUtils;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
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
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Rok, Pedro Lucas nmm. Created on 21/04/2025
 * @project Create Hypertube
 */
public class HyperJunctionBlock extends ActionTubeBlock implements EntityBlock {

    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    public static final BooleanProperty POWERABLE = BooleanProperty.create("powerable");
    public static final BooleanProperty TRAVEL_CONTINUE_POWERED = BooleanProperty.create("travel_continue_powered");


    public HyperJunctionBlock(Properties properties) {
        super(properties);
        registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(OPEN, false)
                .setValue(WATERLOGGED, false)
                .setValue(POWER, 0)
                .setValue(POWERED, false)
                .setValue(POWERABLE, false)
                .setValue(TRAVEL_CONTINUE_POWERED, false)
        );
    }

    @Override
    protected BooleanProperty propertyToUpdate() {
        return TRAVEL_CONTINUE_POWERED;
    }


    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, OPEN, WATERLOGGED, ACTIVE, POWERABLE, POWERED, POWER, TRAVEL_CONTINUE_POWERED);
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
                    .setValue(WATERLOGGED, fluidstate.is(Fluids.WATER))
                    .setValue(POWERABLE, false)
                    .setValue(TRAVEL_CONTINUE_POWERED, false)
                    .setValue(POWER, 0)
                    .setValue(POWERED, false);
        }
        Direction direction = player.getDirection().getOpposite();
        if (player.getXRot() < -45) {
            //direction = Direction.UP;
        } else if (player.getXRot() > 45) {
            //direction = Direction.DOWN;
        }
        return this.defaultBlockState()
                .setValue(FACING, direction)
                .setValue(OPEN, false)
                .setValue(WATERLOGGED, fluidstate.is(Fluids.WATER))
                .setValue(POWERABLE, false)
                .setValue(TRAVEL_CONTINUE_POWERED, false)
                .setValue(POWER, 0)
                .setValue(POWERED, false);
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
        return ModBlocks.HYPER_JUNCTION.asItem();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
        return ModBlockEntities.HYPER_JUNCTION.get().create(blockPos, blockState);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        return (level1, pos, state1, be) -> ((TravelInteractTubeBlockEntity) be).tick();
    }

    @Override
    public List<Direction> getConnectedFaces(BlockState state) {
        return new ArrayList<>(List.of(state.getValue(FACING).getClockWise(), state.getValue(FACING).getCounterClockWise(), state.getValue(FACING)));
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        if (context.getLevel().isClientSide) return InteractionResult.SUCCESS;
        if (context.getPlayer() == null) return InteractionResult.PASS;
        if (super.onWrenched(state, context) == InteractionResult.SUCCESS) return InteractionResult.SUCCESS;

        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof TravelInteractTubeBlockEntity tubeEntity && tubeEntity.isConnected()) {
            if (tubeEntity.getConnectionInDirection(context.getClickedFace()) == null) {
                return InteractionResult.PASS;
            }
            tubeEntity.wrenchClicked(context.getClickedFace());
            updateAfterWrenched(state, context);
            IWrenchable.playRotateSound(context.getLevel(), context.getClickedPos());
            return InteractionResult.SUCCESS;
        }

        level.playSound(player, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.75f, 1);
        return InteractionResult.SUCCESS;
    }

    // ------- Collision Shapes -------
    @Override
    public VoxelShape getShape(BlockState state, @Nullable CollisionContext ctx) {
        if (ctx instanceof EntityCollisionContext ecc
            && ecc.getEntity() != null
            && ecc.getEntity().getPersistentData().getBoolean(TravelConstants.TRAVEL_TAG)) {
            return VoxelUtils.empty();
        }
        return Shapes.block();
    }

    // -------- attachments  --------
    @Override
    public boolean canPlaceAttachment(BlockState state, BlockGetter world, BlockPos pos, Direction side) {
        return side != null && (side != state.getValue(FACING) || side == Direction.DOWN || side == Direction.UP);
    }

}
