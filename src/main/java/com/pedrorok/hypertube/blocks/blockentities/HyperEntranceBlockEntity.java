package com.pedrorok.hypertube.blocks.blockentities;

import com.pedrorok.hypertube.blocks.HyperEntranceBlock;
import com.pedrorok.hypertube.managers.TravelManager;
import com.pedrorok.hypertube.managers.sound.TubeSoundManager;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.Optional;

/**
 * @author Rok, Pedro Lucas nmm. Created on 21/04/2025
 * @project Create Hypertube
 */
public class HyperEntranceBlockEntity extends KineticBlockEntity {

    private static final float RADIUS = 1.0f;

    private static final float SPEED_TO_START = 16;

    @OnlyIn(Dist.CLIENT)
    private TubeSoundManager.TubeAmbientSound tubeAmbientSound = new TubeSoundManager.TubeAmbientSound();

    public HyperEntranceBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }


    @Override
    protected void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(compound, registries, clientPacket);
    }


    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(compound, registries, clientPacket);
    }

    @Override
    public void remove() {
        if (level.isClientSide) {
            tubeAmbientSound.stopSound();
        }
        super.remove();
    }

    @Override
    public void tick() {
        super.tick();
        if (level.isClientSide) {
            tickClient();
            return;
        }
        BlockState state = this.getBlockState();
        BlockPos pos = this.getBlockPos();

        float actualSpeed = Math.abs(this.getSpeed());
        Boolean isOpen = state.getValue(HyperEntranceBlock.OPEN);

        if (actualSpeed < SPEED_TO_START) {
            if (isOpen) {
                level.setBlock(pos, state.setValue(HyperEntranceBlock.OPEN, false), 3);
            }
            return;
        }

        Optional<ServerPlayer> nearbyPlayer = getNearbyPlayers((ServerLevel) level, pos.getCenter());
        if (nearbyPlayer.isEmpty()) {
            if (isOpen) {
                level.setBlock(pos, state.setValue(HyperEntranceBlock.OPEN, false), 3);
            }
            return;
        }
        if (!isOpen) {
            level.setBlock(pos, state.setValue(HyperEntranceBlock.OPEN, true), 3);
        }

        Optional<ServerPlayer> inRangePlayer = getInRangePlayers((ServerLevel) level, pos.getCenter());
        if (inRangePlayer.isEmpty()) return;

        ServerPlayer player = inRangePlayer.get();
        if (player.isShiftKeyDown()) return;
        TravelManager.tryStartTravel(player, pos, state, actualSpeed / 512);
    }


    @OnlyIn(Dist.CLIENT)
    private void tickClient() {

        // this is just for sound
        BlockState state = this.getBlockState();
        BlockPos pos = this.getBlockPos();

        float actualSpeed = Math.abs(this.getSpeed());

        if (actualSpeed < SPEED_TO_START) {
            tubeAmbientSound.tickClientPlayerSounds();
            return;
        }

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

        tubeAmbientSound.enableClientPlayerSound(
                player,
                rotatedDirection,
                distance,
                isOpen
        );
    }


    private Optional<ServerPlayer> getInRangePlayers(ServerLevel level, Vec3 centerPos) {
        return level.players().stream()
                .filter(player -> player.getBoundingBox()
                        .inflate(RADIUS)
                        .contains(centerPos))
                .findFirst();
    }

    private Optional<ServerPlayer> getNearbyPlayers(ServerLevel level, Vec3 centerPos) {
        return level.players().stream()
                .filter(player -> player.getBoundingBox()
                        .inflate(RADIUS * 3)
                        .contains(centerPos))
                .findFirst();
    }
}
