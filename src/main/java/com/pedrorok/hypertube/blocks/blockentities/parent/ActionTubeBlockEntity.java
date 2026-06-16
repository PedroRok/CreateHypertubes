package com.pedrorok.hypertube.blocks.blockentities.parent;

import com.mojang.serialization.Codec;
import com.pedrorok.hypertube.HypertubeMod;
import com.pedrorok.hypertube.core.connection.interfaces.ITubeActionPoint;
import com.pedrorok.hypertube.core.smarttube.ITubeAttachment;
import com.pedrorok.hypertube.core.travel.TravelPathMover;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @author Rok, Pedro Lucas nmm. Created on 11/08/2025
 * @project Create Hypertube
 */
public abstract class ActionTubeBlockEntity extends TravelInteractTubeBlockEntity {



    private final Map<Direction, ITubeAttachment> smartTubeAttachments = new HashMap<>();

    public ActionTubeBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    // --------- Smart Tube Methods ---------
    @Override
    protected void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(compound, registries, clientPacket);

        if (smartTubeAttachments.isEmpty()) return;
        CompoundTag smartTubesTag = new CompoundTag();
        for (Map.Entry<Direction, ITubeAttachment> entry : smartTubeAttachments.entrySet()) {
            smartTubesTag.put(entry.getKey().getSerializedName(), Codec.STRING.write(NbtOps.INSTANCE, entry.getValue().getId()));
        }
        compound.put("attachments", smartTubesTag);
    }

    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(compound, registries, clientPacket);

        smartTubeAttachments.clear();

        if (!compound.contains("attachments", Tag.TAG_COMPOUND)) return;

        CompoundTag smartTubesTag = compound.getCompound("attachments");
        for (Direction direction : Direction.values()) {
            String directionKey = direction.getSerializedName();
            if (!smartTubesTag.contains(directionKey, Tag.TAG_STRING)) continue;

            String smartTubeId = smartTubesTag.getString(directionKey);
            ITubeAttachment smartTube = ITubeAttachment.get(smartTubeId);

            if (smartTube == null) {
                HypertubeMod.LOGGER.error("Failed to load smart tube attachment with id: {} for direction: {}",
                        smartTubeId, direction);
                continue;
            }

            smartTubeAttachments.put(direction, smartTube);
        }
    }

    public void addTubeAttachment(Direction direction, ITubeAttachment smartTube) {
        smartTubeAttachments.put(direction, smartTube);
        setChanged();
        if (level != null && !level.isClientSide) {
            level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public void activateAllTubeAttachments(LivingEntity entity, TravelPathMover travelPathMover, BlockPos pos) {
        for (Map.Entry<Direction, ITubeAttachment> attachmentEntry : smartTubeAttachments.entrySet()) {
            ITubeAttachment value = attachmentEntry.getValue();
            ITubeActionPoint actionPoint = value.getActionPoint(attachmentEntry.getKey());
            if (actionPoint == null) continue;
            actionPoint.handleTravelPath(entity, travelPathMover, pos);
        }
    }

    public ITubeAttachment removeTubeAttachment(Direction direction) {
        ITubeAttachment removedAttachment = smartTubeAttachments.remove(direction);
        if (removedAttachment != null) {
            setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
                level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
            }
        }
        return removedAttachment;
    }

    @Nullable
    public ITubeAttachment getTubeAttachment(Direction direction) {
        return smartTubeAttachments.get(direction);
    }

    public boolean hasTubeAttachment(Direction direction) {
        return smartTubeAttachments.containsKey(direction);
    }

    public boolean hasAnyTubeAttachment() {
        return !smartTubeAttachments.isEmpty();
    }

    public Map<Direction, ITubeAttachment> getTubeAttachments() {
        return smartTubeAttachments;
    }

    public List<Direction> getAttachmentDirectionsNoEmit() {
        return smartTubeAttachments.entrySet().stream().filter(attach -> !attach.getValue().emitRedstoneSignal()).map(Map.Entry::getKey).toList();
    }

    public Set<Direction> getAttachmentDirections() {
        return smartTubeAttachments.keySet();
    }

    public boolean canEmitTo(Direction direction) {
        ITubeAttachment attachment = smartTubeAttachments.get(direction);
        if (attachment == null) return false;
        return attachment.emitRedstoneSignal();
    }
}
