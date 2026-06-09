package com.pedrorok.hypertube.blocks.blockentities;

import com.mojang.serialization.Codec;
import com.pedrorok.hypertube.HypertubeMod;
import com.pedrorok.hypertube.blocks.HyperEntranceBlock;
import com.pedrorok.hypertube.config.ServerConfig;
import com.pedrorok.hypertube.core.connection.interfaces.ITubeActionPoint;
import com.pedrorok.hypertube.core.smarttube.ITubeAttachment;
import com.pedrorok.hypertube.core.sound.TubeSoundManager;
import com.pedrorok.hypertube.core.travel.ItemTravelManager;
import com.pedrorok.hypertube.core.travel.TravelPathMover;
import com.pedrorok.hypertube.registry.ModParticles;
import com.pedrorok.hypertube.registry.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @author Rok, Pedro Lucas nmm. Created on 11/08/2025
 * @project Create Hypertube
 */
public abstract class ActionTubeBlockEntity extends TubeBlockEntity {

    protected static final float RADIUS = 1.0f;
    protected final UUID tubeSoundId = UUID.randomUUID();

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

    public void activateAllTubeAttachmentsForItem(ItemTravelManager.TravelingItem item, BlockPos pos) {
        for (Map.Entry<Direction, ITubeAttachment> attachmentEntry : smartTubeAttachments.entrySet()) {
            ITubeAttachment value = attachmentEntry.getValue();
            ITubeActionPoint actionPoint = value.getActionPoint(attachmentEntry.getKey());
            if (actionPoint == null) continue;
            actionPoint.handleItemTravel(item, level, pos);
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


    // --------------------------------------


    protected void spawnSuctionParticle(Level level, BlockPos blockPos, Direction face) {
        face = face.getOpposite();
        Vec3 center = Vec3.atCenterOf(blockPos);
        RandomSource rand = level.getRandom();

        Vec3 faceNormal = Vec3.atLowerCornerOf(face.getNormal());

        double spread = 0.5;

        Vec3 tangentA = switch (face.getAxis()) {
            case Y, Z -> new Vec3(1, 0, 0);
            default -> new Vec3(0, 1, 0);
        };
        Vec3 tangentB = faceNormal.cross(tangentA).normalize();
        double offsetA = (rand.nextDouble() - 0.5) * 2 * spread;
        double offsetB = (rand.nextDouble() - 0.5) * 2 * spread;
        Vec3 randomOffset = tangentA.scale(offsetA).add(tangentB.scale(offsetB));

        Vec3 start = center.add(faceNormal.scale(1 + level.random.nextFloat())).add(randomOffset);
        Vec3 motion = center.subtract(start).normalize().scale(0.05);


        level.addParticle(
                ModParticles.SUCTION_PARTICLE.get(),
                start.x, start.y, start.z,
                motion.x, motion.y, motion.z
        );
    }


    protected void playOpenCloseSound(boolean open) {
        RandomSource random = level.random;
        float pitch = 0.4F + random.nextFloat() * 0.4F;
        level.playSound(null, this.getBlockPos(), open ? ModSounds.HYPERTUBE_ENTRANCE_OPEN.get() : ModSounds.HYPERTUBE_ENTRANCE_CLOSE.get(), SoundSource.BLOCKS, 0.2f, pitch);
    }


    @OnlyIn(Dist.CLIENT)
    protected void playClientEffects(TubeSoundManager.TubeAmbientSound sound) {
        BlockState state = this.getBlockState();
        BlockPos pos = this.getBlockPos();

        boolean isOpen = state.getValue(HyperEntranceBlock.OPEN);

        LocalPlayer player = Minecraft.getInstance().player;
        Vec3 source = pos.getCenter();
        Vec3 listener = player.position();

        Vec3 worldDirection = source.subtract(listener).normalize();

        Vec3 forward = player.getLookAngle().normalize();
        Vec3 up = player.getUpVector(1.0F).normalize();
        Vec3 right = forward.cross(up).normalize();

        double x = worldDirection.dot(right);
        double y = worldDirection.dot(up);
        double z = worldDirection.dot(forward);

        Vec3 rotatedDirection = new Vec3(x, y, z).normalize();

        double distance = player.distanceToSqr(source);

        if (isOpen) {
            spawnSuctionParticle(level, pos, state.getValue(HyperEntranceBlock.FACING));
        }

        sound.enableClientPlayerSound(
                player,
                rotatedDirection,
                distance,
                isOpen
        );
    }

    @Nullable
    protected LivingEntity getInRangeLivingEntities(ServerLevel level, Vec3 centerPos, Direction facing) {
        Vec3 checkPos = centerPos.add(Vec3.atLowerCornerOf(facing.getOpposite().getNormal()));

        return level.getNearestEntity(
                level.getEntitiesOfClass(LivingEntity.class,
                        AABB.ofSize(checkPos, (RADIUS - 0.25) * 2, (RADIUS - 0.25) * 2, (RADIUS - 0.25) * 2),
                        (entity) -> ServerConfig.canEntityTravel(entity.getType())),
                TargetingConditions.forNonCombat().ignoreLineOfSight(),
                null,
                centerPos.x, centerPos.y, centerPos.z);
    }

    @Nullable
    protected LivingEntity getNearbyLivingEntities(ServerLevel level, Vec3 centerPos) {
        return level.getNearestEntity(
                level.getEntitiesOfClass(LivingEntity.class,
                        AABB.ofSize(centerPos, RADIUS * 6, RADIUS * 6, RADIUS * 6),
                        (entity) -> ServerConfig.canEntityTravel(entity.getType())),
                TargetingConditions.forNonCombat().ignoreLineOfSight(),
                null,
                centerPos.x, centerPos.y, centerPos.z);
    }

    protected boolean hasNearbyItems(ServerLevel level, Vec3 centerPos) {
        return !level.getEntitiesOfClass(ItemEntity.class,
                AABB.ofSize(centerPos, RADIUS * 6, RADIUS * 6, RADIUS * 6),
                item -> !item.getItem().isEmpty() && !ItemTravelManager.isItemTraveling(item.getUUID()))
                .isEmpty();
    }


    /**
     * @return true if the tube is closed, false if it was opened
     */
    protected boolean isTubeClosed(boolean canOpen, boolean isOpen) {
        BlockState state = this.getBlockState();
        BlockPos pos = this.getBlockPos();
        if (!canOpen) {
            if (isOpen) {
                level.setBlock(pos, state.setValue(HyperEntranceBlock.OPEN, false), 3);
                playOpenCloseSound(false);
            }
            return true;
        }

        if (!isOpen) {
            level.setBlock(pos, state.setValue(HyperEntranceBlock.OPEN, true), 3);
            playOpenCloseSound(true);
        }
        return false;
    }


    @Override
    public void remove() {
        if (level.isClientSide) {
            removeClient();
        }
        super.remove();
    }

    @OnlyIn(Dist.CLIENT)
    private void removeClient() {
        TubeSoundManager.getAmbientSound(tubeSoundId).stopSound();
        TubeSoundManager.removeAmbientSound(tubeSoundId);
    }
}
