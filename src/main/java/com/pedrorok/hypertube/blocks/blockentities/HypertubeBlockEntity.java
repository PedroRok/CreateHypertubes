package com.pedrorok.hypertube.blocks.blockentities;

import com.pedrorok.hypertube.blocks.HypertubeBlock;
import com.pedrorok.hypertube.blocks.IBezierProvider;
import com.pedrorok.hypertube.managers.placement.BezierConnection;
import com.pedrorok.hypertube.managers.placement.SimpleConnection;
import com.simibubi.create.api.contraption.transformable.TransformableBlockEntity;
import com.simibubi.create.content.contraptions.StructureTransform;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Rok, Pedro Lucas nmm. Created on 24/04/2025
 * @project Create Hypertube
 */
@Getter
public class HypertubeBlockEntity extends BlockEntity implements TransformableBlockEntity, IBezierProvider {

    private BezierConnection connectionTo;
    private SimpleConnection connectionFrom;

    public HypertubeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public void setConnectionTo(BezierConnection connection) {
        this.connectionTo = connection;
        if (level != null && !level.isClientSide()) {
            if (level.getBlockState(worldPosition).getBlock() instanceof HypertubeBlock hypertubeBlock) {
                hypertubeBlock.updateBlockStateFromEntity(level, worldPosition);
            }
        }
        setChanged();
        sync();
    }

    public void setConnectionFrom(SimpleConnection connectionFrom, Direction direction) {
        this.connectionFrom = connectionFrom;

        if (level != null && !level.isClientSide()) {
            if (level.getBlockState(worldPosition).getBlock() instanceof HypertubeBlock hypertubeBlock) {
                hypertubeBlock.updateBlockStateFromEntity(level, worldPosition);
                if (direction != null) {
                    BlockState state = hypertubeBlock.getState(List.of(direction));
                    hypertubeBlock.updateBlockState(level, worldPosition, state);
                }
            }
        }
        setChanged();
        sync();
    }

    public boolean isConnected() {
        return connectionTo != null || connectionFrom != null;
    }

    public void sync() {
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        writeConnection(tag);
    }

    private void writeConnection(CompoundTag tag) {
        if (connectionTo != null) {
            tag.put("ConnectionTo", BezierConnection.CODEC.encodeStart(NbtOps.INSTANCE, connectionTo)
                    .getOrThrow());
        }
        if (connectionFrom != null) {
            tag.put("ConnectionFrom", SimpleConnection.CODEC.encodeStart(NbtOps.INSTANCE, connectionFrom)
                    .getOrThrow());
        }
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);

        if (tag.contains("ConnectionTo")) {
            this.connectionTo = BezierConnection.CODEC.parse(NbtOps.INSTANCE, tag.get("ConnectionTo"))
                    .getOrThrow();
        }
        if (tag.contains("ConnectionFrom")) {
            this.connectionFrom = SimpleConnection.CODEC.parse(NbtOps.INSTANCE, tag.get("ConnectionFrom"))
                    .getOrThrow();
        }
    }

    public List<Direction> getFacesConnectable() {
        if (connectionTo != null && connectionFrom != null) return List.of();
        if (connectionTo != null) {
            return List.of(connectionTo.getFromPos().direction().getOpposite());
        }
        if (connectionFrom != null) {
            BlockEntity blockEntity = level.getBlockEntity(connectionFrom.pos());
            if (blockEntity instanceof HypertubeBlockEntity hypertubeBlockEntity
                && hypertubeBlockEntity.getConnectionTo() != null
                && hypertubeBlockEntity.getConnectionTo().getToPos().pos().equals(this.worldPosition)) {
                return List.of(hypertubeBlockEntity.getConnectionTo().getToPos().direction());
            }
        }

        Boolean eastWest = getBlockState().getValue(HypertubeBlock.EAST_WEST);
        if (eastWest) {
            List<Direction> directions = new ArrayList<>();
            boolean hasBlockInEast = level.getBlockState(worldPosition.relative(Direction.EAST)).getBlock() instanceof HypertubeBlock;
            if (!hasBlockInEast) {
                directions.add(Direction.EAST);
            }
            boolean hasBlockInWest = level.getBlockState(worldPosition.relative(Direction.WEST)).getBlock() instanceof HypertubeBlock;
            if (!hasBlockInWest) {
                directions.add(Direction.WEST);
            }
            return directions;
        }
        Boolean upDown = getBlockState().getValue(HypertubeBlock.UP_DOWN);
        if (upDown) {
            List<Direction> directions = new ArrayList<>();
            boolean hasBlockInUp = level.getBlockState(worldPosition.relative(Direction.UP)).getBlock() instanceof HypertubeBlock;
            if (!hasBlockInUp) {
                directions.add(Direction.UP);
            }
            boolean hasBlockInDown = level.getBlockState(worldPosition.relative(Direction.DOWN)).getBlock() instanceof HypertubeBlock;
            if (!hasBlockInDown) {
                directions.add(Direction.DOWN);
            }
            return directions;
        }
        Boolean northSouth = getBlockState().getValue(HypertubeBlock.NORTH_SOUTH);
        if (northSouth) {
            List<Direction> directions = new ArrayList<>();
            boolean hasBlockInNorth = level.getBlockState(worldPosition.relative(Direction.NORTH)).getBlock() instanceof HypertubeBlock;
            if (!hasBlockInNorth) {
                directions.add(Direction.NORTH);
            }
            boolean hasBlockInSouth = level.getBlockState(worldPosition.relative(Direction.SOUTH)).getBlock() instanceof HypertubeBlock;
            if (!hasBlockInSouth) {
                directions.add(Direction.SOUTH);
            }
            return directions;
        }

        return List.of(Direction.values());
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public @NotNull ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(@NotNull Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.@NotNull Provider registries) {
        CompoundTag tag = pkt.getTag();
        loadAdditional(tag, registries);
    }

    @Override
    public void transform(BlockEntity blockEntity, StructureTransform transform) {
    }

    @Override
    public BezierConnection getBezierConnection() {
        return connectionTo;
    }
}