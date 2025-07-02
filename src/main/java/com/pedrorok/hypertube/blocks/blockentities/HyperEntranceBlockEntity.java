package com.pedrorok.hypertube.blocks.blockentities;

import com.pedrorok.hypertube.HypertubeMod;
import com.pedrorok.hypertube.blocks.HyperEntranceBlock;
import com.pedrorok.hypertube.core.connection.BezierConnection;
import com.pedrorok.hypertube.core.connection.SimpleConnection;
import com.pedrorok.hypertube.core.connection.TubeConnectionException;
import com.pedrorok.hypertube.core.connection.interfaces.IConnection;
import com.pedrorok.hypertube.core.connection.interfaces.ITubeConnectionEntity;
import com.pedrorok.hypertube.core.sound.TubeSoundManager;
import com.pedrorok.hypertube.core.travel.TravelConstants;
import com.pedrorok.hypertube.core.travel.TravelManager;
import com.pedrorok.hypertube.registry.ModParticles;
import com.pedrorok.hypertube.registry.ModSounds;
import com.simibubi.create.api.equipment.goggles.IHaveHoveringInformation;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.fan.AirFlowParticleData;
import com.simibubi.create.content.kinetics.fan.EncasedFanBlockEntity;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EndRodParticle;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author Rok, Pedro Lucas nmm. Created on 21/04/2025
 * @project Create Hypertube
 */
public class HyperEntranceBlockEntity extends KineticBlockEntity implements IHaveHoveringInformation, ITubeConnectionEntity {

    private static final float RADIUS = 1.0f;
    private static final float SPEED_TO_START = 16;
    private final UUID tubeSoundId = UUID.randomUUID();

    @Getter
    private IConnection connection;

    public HyperEntranceBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    // --------- Nbt Methods ---------
    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(compound, registries, clientPacket);
        if (compound.contains("Connection")) {
            connection = getConnection(compound, "Connection");
        }
    }

    @Override
    protected void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(compound, registries, clientPacket);
        writeConnection(compound, new Tuple<>(connection, "Connection"));
    }
    // --------- Nbt Methods ---------

    // --------- Tube Segment Methods ---------
    public boolean wrenchClicked(Direction direction) {
        IConnection connectionInDirection = getConnectionInDirection(direction);
        if (connectionInDirection == null) return false;
        connectionInDirection.updateTubeSegments(level);
        return true;
    }
    // --------- Tube Segment Methods ---------

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

    @Override
    public void tick() {
        super.tick();
        Boolean isBlocked = getBlockState().getValue(HyperEntranceBlock.IN_FRONT);
        if (level.isClientSide) {
            tickClient(isBlocked);
            return;
        }
        if (isBlocked) {
            return;
        }

        BlockState state = this.getBlockState();
        BlockPos pos = this.getBlockPos();

        float actualSpeed = Math.abs(this.getSpeed());
        Boolean isOpen = state.getValue(HyperEntranceBlock.OPEN);
        if (actualSpeed < SPEED_TO_START) {
            if (isOpen) {
                level.setBlock(pos, state.setValue(HyperEntranceBlock.OPEN, false), 3);
                playOpenCloseSound(false);
            }
            return;
        }
        Boolean isLocked = getBlockState().getValue(HyperEntranceBlock.LOCKED);

        LivingEntity nearbyEntity = getNearbyLivingEntities((ServerLevel) level, pos.getCenter());
        boolean isPlayer = nearbyEntity instanceof ServerPlayer;
        ServerPlayer nearbyPlayer = isPlayer ? (ServerPlayer) nearbyEntity : null;
        boolean canOpen = nearbyEntity != null
                          && (!isLocked
                              || nearbyEntity.isShiftKeyDown()
                              || (isPlayer && nearbyPlayer.connection.latency() > TravelConstants.LATENCY_THRESHOLD)
                              || nearbyEntity.getPersistentData().getBoolean(TravelConstants.TRAVEL_TAG));

        if (!canOpen) {
            if (isOpen) {
                level.setBlock(pos, state.setValue(HyperEntranceBlock.OPEN, false), 3);
                playOpenCloseSound(false);
            }
            return;
        }
        if (!isOpen) {
            level.setBlock(pos, state.setValue(HyperEntranceBlock.OPEN, true), 3);
            playOpenCloseSound(true);
        }
        LivingEntity inRangeEntity = getInRangeLivingEntities((ServerLevel) level,
                pos.getCenter(),
                state.getValue(HyperEntranceBlock.FACING));
        if (inRangeEntity == null) return;

        boolean isPlayerInRange = inRangeEntity instanceof ServerPlayer;
        ServerPlayer player = isPlayerInRange ? (ServerPlayer) inRangeEntity : null;
        if (!isLocked
            && (inRangeEntity.isShiftKeyDown() && (isPlayerInRange && player.connection.latency() <= TravelConstants.LATENCY_THRESHOLD)))
            return;
        TravelManager.tryStartTravel(inRangeEntity, pos, state, actualSpeed / 512);
    }


    @OnlyIn(Dist.CLIENT)
    private void tickClient(boolean isBlocked) {

        BlockState state = this.getBlockState();
        BlockPos pos = this.getBlockPos();

        float actualSpeed = Math.abs(this.getSpeed());

        TubeSoundManager.TubeAmbientSound sound = TubeSoundManager.getAmbientSound(tubeSoundId);
        if (actualSpeed < SPEED_TO_START || isBlocked) {
            sound.tickClientPlayerSounds();
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
    private LivingEntity getInRangeLivingEntities(ServerLevel level, Vec3 centerPos, Direction facing) {
        Vec3 checkPos = centerPos.add(Vec3.atLowerCornerOf(facing.getOpposite().getNormal()));

        return level.getNearestEntity(
                level.getEntitiesOfClass(LivingEntity.class,
                        AABB.ofSize(checkPos, (RADIUS - 0.25) * 2, (RADIUS - 0.25) * 2, (RADIUS - 0.25) * 2),
                        (entity) -> TravelConstants.TRAVELLER_ENTITIES.contains(entity.getType())),
                TargetingConditions.forNonCombat().ignoreLineOfSight(),
                null,
                centerPos.x, centerPos.y, centerPos.z);
    }

    @Nullable
    private LivingEntity getNearbyLivingEntities(ServerLevel level, Vec3 centerPos) {
        return level.getNearestEntity(
                level.getEntitiesOfClass(LivingEntity.class,
                        AABB.ofSize(centerPos, RADIUS * 6, RADIUS * 6, RADIUS * 6),
                        (entity) -> TravelConstants.TRAVELLER_ENTITIES.contains(entity.getType())),
                TargetingConditions.forNonCombat().ignoreLineOfSight(),
                null,
                centerPos.x, centerPos.y, centerPos.z);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        float finalSpeed = Math.abs(this.getSpeed());
        IRotate.SpeedLevel.getFormattedSpeedText(speed, finalSpeed < SPEED_TO_START)
                .forGoggles(tooltip);

        if (getBlockState().getValue(HyperEntranceBlock.IN_FRONT)) {
            tooltip.add(Component.literal("     ")
                    .append(Component.translatable("tooltip.create_hypertube.entrance_blocked")
                            .withColor(0xFF0000)));
        } else if (finalSpeed < SPEED_TO_START) {
            tooltip.add(Component.literal("     ")
                    .append(Component.literal("\u2592 "))
                    .append(Component.translatable("tooltip.create_hypertube.entrance_no_speed"))
                    .withColor(0xFF0000));
        }
        return true;
    }

    @Override
    public boolean addToTooltip(List<Component> tooltip, boolean isPlayerSneaking) {

        if (getBlockState().getValue(HyperEntranceBlock.LOCKED)
            && Math.abs(this.getSpeed()) >= SPEED_TO_START) {
            tooltip.add(Component.literal("     ")
                    .append(Component.translatable("block.hypertube.hyper_entrance.sneak_to_enter"))
                    .withColor(0xFFFFFF));
        }
        return true;
    }

    private void playOpenCloseSound(boolean open) {
        RandomSource random = level.random;
        float pitch = 0.4F + random.nextFloat() * 0.4F;
        level.playSound(null, this.getBlockPos(), open ? ModSounds.HYPERTUBE_ENTRANCE_OPEN.get() : ModSounds.HYPERTUBE_ENTRANCE_CLOSE.get(), SoundSource.BLOCKS, 0.2f, pitch);
    }

    @Override
    public @Nullable IConnection getConnectionInDirection(Direction direction) {
        SimpleConnection sameConnectionBlockPos = IConnection.getSameConnectionBlockPos(connection, level, worldPosition);
        if (sameConnectionBlockPos != null) {
            Direction thisConn = sameConnectionBlockPos.direction();
            if (thisConn != null && thisConn.equals(direction)) {
                return connection;
            }
        }
        return null;
    }

    @Override
    public @Nullable IConnection getThisConnectionFrom(SimpleConnection connection) {
        if (this.connection instanceof BezierConnection bezierConnection) {
            if (connection.isSameConnection(bezierConnection.getFromPos()))
                return bezierConnection;
        }
        return null;
    }

    @Override
    public boolean hasConnectionAvailable() {
        return connection == null;
    }

    @Override
    public boolean isConnected() {
        return connection != null;
    }

    @Override
    public void setConnection(IConnection connection, Direction thisConnectionDir) {
        if (this.connection == null) {
            this.connection = connection;
        } else {
            HypertubeMod.LOGGER.error(new TubeConnectionException("Connection could not define connection", this.connection, connection).getMessage());
            return;
        }
        setChanged();
        sync();
    }

    @Override
    public void clearConnection(IConnection connection) {
        if (this.connection != null && this.connection.isSameConnection(connection)) {
            this.connection = null;
        } else {
            HypertubeMod.LOGGER.error(new TubeConnectionException("Connection could not be cleared", this.connection, connection).getMessage());
            return;
        }
        setChanged();
        sync();
    }

    @Override
    public int blockBroken() {
        int toDrop = 0;
        if (connection != null) {
            toDrop = blockBroken(level, connection, worldPosition);
        }
        return toDrop;
    }

    @Override
    public List<Direction> getFacesConnectable() {
        if (connection != null)
            return List.of();
        return List.of(getBlockState().getValue(HyperEntranceBlock.FACING));
    }

    @Override
    public List<IConnection> getConnections() {
        List<IConnection> connections = new ArrayList<>();
        if (connection != null) {
            connections.add(connection);
        }
        return connections;
    }

    public void sync() {
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    public static void spawnSuctionParticle(Level level, BlockPos blockPos, Direction face) {
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
}
