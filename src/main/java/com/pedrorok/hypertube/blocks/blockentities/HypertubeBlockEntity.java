package com.pedrorok.hypertube.blocks.blockentities;

import com.pedrorok.hypertube.managers.placement.BezierConnection;
import com.pedrorok.hypertube.registry.ModBlockEntities;
import com.simibubi.create.api.contraption.transformable.TransformableBlockEntity;
import com.simibubi.create.content.contraptions.StructureTransform;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import lombok.Getter;
import net.createmod.catnip.animation.LerpedFloat;
import net.minecraft.core.BlockPos;
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
public class HypertubeBlockEntity extends BlockEntity implements TransformableBlockEntity {

    private BezierConnection connection;

    public HypertubeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.HYPERTUBE_ENTITY.get(), pos, state);
    }

    public void setConnection(BezierConnection connection) {
        this.connection = connection;
        setChanged();

        // Força sincronização com o cliente
        sync();
    }

    // Método para sincronizar dados com o cliente
    public void sync() {
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    public static <T extends BlockEntity> void tick(Level level, BlockPos pos, BlockState state, T t) {
        if (!level.isClientSide) return;
        if (t == null) return;
        if (!(t instanceof HypertubeBlockEntity tubeState)) return;
        if (tubeState.getConnection() == null) return;
        if (!tubeState.getConnection().isValid()) return;
        tubeState.getConnection().drawPath(LerpedFloat.linear()
                .startWithValue(0));
    }



    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        writeConnection(tag);
    }

    private void writeConnection(CompoundTag tag) {
        if (connection == null) return;
        tag.put("Connection", BezierConnection.CODEC.encodeStart(NbtOps.INSTANCE, connection)
                .getOrThrow());
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);

        if (!tag.contains("Connection")) return;
        this.connection = BezierConnection.CODEC.parse(NbtOps.INSTANCE, tag.get("Connection"))
                .getOrThrow();
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
        // Implementação da transformação se necessário
    }

    /*
     * TODO: test with SmartBlockEntity
    @Override
    @OnlyIn(Dist.CLIENT)
    public AABB getRenderBoundingBox() {
        return AABB.INFINITE;
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        // Adicione comportamentos se necessário
    }
     */
}