package com.pedrorok.hypertube.blocks;

import com.pedrorok.hypertube.blocks.blockentities.HyperAcceleratorBlockEntity;
import com.pedrorok.hypertube.core.connection.interfaces.ITubeActionPoint;
import com.pedrorok.hypertube.core.sound.TubeSoundManager;
import com.pedrorok.hypertube.core.travel.TravelConstants;
import com.pedrorok.hypertube.core.travel.TravelPathMover;
import com.pedrorok.hypertube.network.NetworkHandler;
import com.pedrorok.hypertube.network.packets.SpeedChangePacket;
import com.pedrorok.hypertube.registry.ModBlockEntities;
import com.pedrorok.hypertube.registry.ModBlocks;
import com.pedrorok.hypertube.utils.MessageUtils;
import com.pedrorok.hypertube.utils.TubeUtils;
import com.pedrorok.hypertube.utils.VoxelUtils;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
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
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Rok, Pedro Lucas nmm. Created on 21/04/2025
 * @project Create Hypertube
 */
public class HyperAcceleratorBlock extends TubeBlock implements EntityBlock, ICogWheel, ITubeActionPoint {

    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    public static final BooleanProperty ACCELERATE = BooleanProperty.create("accelerate");

    public HyperAcceleratorBlock(Properties properties) {
        super(properties);
        registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(OPEN, false)
                .setValue(WATERLOGGED, false)
                .setValue(ACTIVE, false)
                .setValue(ACCELERATE, true));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, OPEN, WATERLOGGED, ACTIVE, ACCELERATE);
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
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(FACING).getAxis();
    }

    @Override
    public Item getItem() {
        return ModBlocks.HYPER_ACCELERATOR.asItem();
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
        BlockEntity blockEntity = context.getLevel().getBlockEntity(context.getClickedPos());
        if (blockEntity instanceof HyperAcceleratorBlockEntity entrance) {
            if (entrance.wrenchClicked(context.getClickedFace())) {
                IWrenchable.playRotateSound(context.getLevel(), context.getClickedPos());
                return InteractionResult.SUCCESS;
            }
        }
        Player player = context.getPlayer();
        BlockState blockState = state.setValue(ACCELERATE, !state.getValue(ACCELERATE));
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        level.setBlock(pos, blockState, 3);

        if (blockState.getValue(ACCELERATE)) {
            MessageUtils.sendActionMessage(player,
                    Component.translatable("block.hypertube.hyper_accelerator.accelerate_mode")
                            .withStyle(ChatFormatting.YELLOW), true);
        } else {
            MessageUtils.sendActionMessage(player,
                    Component.translatable("block.hypertube.hyper_accelerator.brake_mode")
                            .withStyle(ChatFormatting.GOLD), true);
        }
        IWrenchable.playRotateSound(context.getLevel(), context.getClickedPos());
        return InteractionResult.SUCCESS;
    }

    @Override
    public void handleTravelPath(LivingEntity entity, TravelPathMover mover, BlockPos pos) {
        Level level = entity.level();
        HyperAcceleratorBlockEntity tube = (HyperAcceleratorBlockEntity) level.getBlockEntity(pos);
        if (tube == null || mover == null) return;
        float speed = TubeUtils.calculateTravelSpeed(Math.abs(tube.getSpeed())) / 2;
        float newSpeed = mover.getTravelSpeed() + speed * (tube.getBlockState().getValue(ACCELERATE) ? 1 : -1);
        newSpeed = Math.max(0.4333f, newSpeed);
        mover.setTravelSpeed(newSpeed);
        NetworkHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity),
                new SpeedChangePacket(entity.getId(), newSpeed));
        TubeSoundManager.playTubeSuctionSound(entity, entity.position());
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

    @Override
    public boolean isSmallCog() {
        return true;
    }
}
