package com.pedrorok.hypertube.blocks;

import com.pedrorok.hypertube.blocks.blockentities.HyperEntranceBlockEntity;
import com.pedrorok.hypertube.managers.TravelManager;
import com.pedrorok.hypertube.registry.ModBlockEntities;
import com.pedrorok.hypertube.utils.VoxelUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
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
public class HyperEntranceBlock extends HypertubeBaseBlock implements EntityBlock, TubeConnection {

    public static DirectionProperty FACING = DirectionProperty.create("facing", Direction.values());

    public static final VoxelShape SHAPE_NORTH = Block.box(0D, 0D, 0D, 16D, 16D, 10D);
    public static final VoxelShape SHAPE_SOUTH = Block.box(0D, 0D, 6D, 16D, 16D, 16D);
    public static final VoxelShape SHAPE_EAST = Block.box(6D, 0D, 0D, 16D, 16D, 16D);
    public static final VoxelShape SHAPE_WEST = Block.box(0D, 0D, 0D, 10D, 16D, 16D);
    public static final VoxelShape SHAPE_UP = Block.box(0D, 6D, 0D, 16D, 16D, 16D);
    public static final VoxelShape SHAPE_DOWN = Block.box(0D, 0D, 0D, 16D, 10D, 16D);

    public HyperEntranceBlock() {
        super(HypertubeBlock.PROPERTIES);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(@NotNull BlockPlaceContext pContext) {
        BlockState state = super.getStateForPlacement(pContext);
        if (state == null) return null;
        Direction direction = pContext.getClickedFace();
        return state.setValue(FACING, direction.getOpposite());
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, @Nullable CollisionContext ctx) {
        if (ctx instanceof EntityCollisionContext ecc
            && ecc.getEntity() instanceof Player player
            && player.getPersistentData().getBoolean(TravelManager.TRAVEL_TAG)) {
            return VoxelUtils.empty();
        }

        return switch (state.getValue(FACING)) {
            case NORTH -> SHAPE_NORTH;
            case SOUTH -> SHAPE_SOUTH;
            case EAST -> SHAPE_EAST;
            case WEST -> SHAPE_WEST;
            case UP -> SHAPE_UP;
            case DOWN -> SHAPE_DOWN;
        };
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        return type == ModBlockEntities.HYPERTUBE_ENTRANCE_ENTITY.get() ? HyperEntranceBlockEntity::tick : null;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
        return ModBlockEntities.HYPERTUBE_ENTRANCE_ENTITY.get().create(blockPos, blockState);
    }
}
