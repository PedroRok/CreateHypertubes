package com.pedrorok.hypertube.blocks.blockentities;

import com.pedrorok.hypertube.blocks.HypertubeBlock;
import com.pedrorok.hypertube.blocks.IBezierProvider;
import com.pedrorok.hypertube.managers.placement.BezierConnection;
import com.pedrorok.hypertube.managers.placement.SimpleConnection;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author Rok, Pedro Lucas nmm. Created on 24/04/2025
 * @project Create Hypertube
 */
@Getter
public class HypertubeBlockEntity extends BlockEntity implements IBezierProvider {

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
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        writeConnection(tag);
    }

    private void writeConnection(CompoundTag tag) {
        if (connectionTo != null) {
            tag.put("ConnectionTo", BezierConnection.CODEC.encodeStart(NbtOps.INSTANCE, connectionTo)
                    .get().orThrow());
        }
        if (connectionFrom != null) {
            tag.put("ConnectionFrom", SimpleConnection.CODEC.encodeStart(NbtOps.INSTANCE, connectionFrom)
                    .get().orThrow());
        }
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);

        if (tag.contains("ConnectionTo")) {
            this.connectionTo = BezierConnection.CODEC.parse(NbtOps.INSTANCE, tag.get("ConnectionTo"))
                    .get().orThrow();
        }
        if (tag.contains("ConnectionFrom")) {
            this.connectionFrom = SimpleConnection.CODEC.parse(NbtOps.INSTANCE, tag.get("ConnectionFrom"))
                    .get().orThrow();
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
        return List.of(Direction.values());
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public @NotNull ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag tag = pkt.getTag();
        if (tag == null) {
            return;
        }
        load(tag);
    }

    @Override
    public BezierConnection getBezierConnection() {
        return connectionTo;
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition).inflate(512);
    }
}