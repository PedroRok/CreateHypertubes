package com.pedrorok.hypertube.blocks.blockentities;

import com.pedrorok.hypertube.blocks.HypertubeBlock;
import com.pedrorok.hypertube.blocks.IBezierProvider;
import com.pedrorok.hypertube.managers.placement.BezierConnection;
import com.pedrorok.hypertube.managers.placement.SimpleConnection;
import com.pedrorok.hypertube.registry.ModBlockEntities;
import com.simibubi.create.api.contraption.transformable.TransformableBlockEntity;
import com.simibubi.create.content.contraptions.StructureTransform;
import com.simibubi.create.foundation.blockEntity.CachedRenderBBBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import lombok.Getter;
import net.createmod.catnip.animation.LerpedFloat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author Rok, Pedro Lucas nmm. Created on 24/04/2025
 * @project Create Hypertube
 */
@Getter
public class HypertubeBlockEntity extends BlockEntity implements TransformableBlockEntity, IBezierProvider {

    private BezierConnection connectionTo;
    private SimpleConnection connectionFrom;

    public HypertubeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.HYPERTUBE_ENTITY.get(), pos, state);
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

    public void setConnectionFrom(SimpleConnection connectionFrom) {
        this.connectionFrom = connectionFrom;
        setChanged();

        sync();
    }

    public void sync() {
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    public static <T extends BlockEntity> void tick(Level level, BlockPos pos, BlockState state, T t) {
        //if (!level.isClientSide) return;
        // NOTE: THIS IS TEMPORARY
        //if (t == null) return;
        //if (!(t instanceof HypertubeBlockEntity tubeEntity)) return;


        //if (tubeEntity.getConnectionTo() == null) return;
        //if (!tubeEntity.getConnectionTo().isValid()) return;
        //tubeEntity.getConnectionTo().drawPath(LerpedFloat.linear()
        //        .startWithValue(0));
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
        if (connectionFrom != null) {
            BlockEntity blockEntity = level.getBlockEntity(connectionFrom.pos());
            if (blockEntity instanceof HypertubeBlockEntity hypertubeBlockEntity && hypertubeBlockEntity.getConnectionTo() != null) {
                SimpleConnection toPos = hypertubeBlockEntity.getConnectionTo().getToPos();
                return List.of(toPos.direction(),
                        toPos.direction().getOpposite());
            }
        }
        if (connectionTo != null) {
            return List.of(connectionTo.getFromPos().direction(), connectionTo.getFromPos().direction().getOpposite());
        }
        BlockState blockState = getBlockState();
        if (!(blockState.getBlock() instanceof HypertubeBlock hypertubeBlock)) return List.of();
        List<Direction> connectedFaces = hypertubeBlock.getConnectedFaces(blockState);
        return connectedFaces.isEmpty() ? List.of(Direction.values()) : connectedFaces.stream().map(Direction::getOpposite).toList();
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