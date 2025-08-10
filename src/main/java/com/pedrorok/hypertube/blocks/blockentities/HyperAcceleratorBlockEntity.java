package com.pedrorok.hypertube.blocks.blockentities;

import com.pedrorok.hypertube.HypertubeMod;
import com.pedrorok.hypertube.blocks.HyperAcceleratorBlock;
import com.pedrorok.hypertube.blocks.HyperEntranceBlock;
import com.pedrorok.hypertube.blocks.HypertubeBlock;
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
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
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
public class HyperAcceleratorBlockEntity extends KineticBlockEntity implements IHaveHoveringInformation, ITubeConnectionEntity {

    private static final float RADIUS = 1.0f;

    private final UUID tubeSoundId = UUID.randomUUID();

    @Getter
    private IConnection connectionOne;
    @Getter
    private IConnection connectionTwo;

    public HyperAcceleratorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    // --------- Nbt Methods ---------
    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(compound, registries, clientPacket);
        if (compound.contains("ConnectionOne")) {
            connectionOne = getConnection(compound, "ConnectionOne");
        }
        if (compound.contains("ConnectionTwo")) {
            connectionTwo = getConnection(compound, "ConnectionTwo");
        }
    }

    @Override
    protected void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(compound, registries, clientPacket);
        writeConnection(compound, new Tuple<>(connectionOne, "ConnectionOne"), new Tuple<>(connectionTwo, "ConnectionTwo"));
    }
    // --------- Nbt Methods ---------

    // --------- Tube Segment Methods ---------
    public boolean wrenchClicked(Direction direction) {
        IConnection connectionInDirection = getConnectionInDirection(direction);
        if (connectionInDirection == null) {
            if (connectionOne != null) {
                connectionOne.updateTubeSegments(level);
            }
            if (connectionTwo != null) {
                connectionTwo.updateTubeSegments(level);
            }
            return true;
        }
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
        if (level.isClientSide) {
            tickClient();
            return;
        }

        BlockState state = this.getBlockState();
        BlockPos pos = this.getBlockPos();

        float actualSpeed = Math.abs(this.getSpeed());
        Boolean isOpen = state.getValue(HyperAcceleratorBlock.OPEN);

        LivingEntity nearbyEntity = getNearbyLivingEntities((ServerLevel) level, pos.getCenter());

        boolean canOpen = nearbyEntity != null && nearbyEntity.getPersistentData().getBoolean(TravelConstants.TRAVEL_TAG);

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

        if (!inRangeEntity.isShiftKeyDown() &&
            !inRangeEntity.getPersistentData().getBoolean(TravelConstants.TRAVEL_TAG)) {
            return;
        }
    }


    @OnlyIn(Dist.CLIENT)
    private void tickClient() {

        BlockState state = this.getBlockState();
        BlockPos pos = this.getBlockPos();


        TubeSoundManager.TubeAmbientSound sound = TubeSoundManager.getAmbientSound(tubeSoundId);

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
            HyperEntranceBlockEntity.spawnSuctionParticle(level, pos, state.getValue(HyperAcceleratorBlock.FACING));
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


        int SPEED_TO_START = 16;

        IRotate.SpeedLevel.getFormattedSpeedText(speed, finalSpeed < SPEED_TO_START)
                .forGoggles(tooltip);

        if (finalSpeed < SPEED_TO_START) {
            tooltip.add(Component.literal("     ")
                    .append(Component.literal("\u2592 "))
                    .append(Component.translatable("tooltip.create_hypertube.entrance_no_speed"))
                    .withColor(0xFF0000));
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
        if (getConnectionDirection(direction, connectionOne)) return connectionOne;
        if (getConnectionDirection(direction, connectionTwo)) return connectionTwo;
        return null;
    }

    private boolean getConnectionDirection(Direction direction, IConnection connection) {
        if (connection != null) {
            SimpleConnection sameConnectionBlockPos = IConnection.getSameConnectionBlockPos(connection, level, worldPosition);
            if (sameConnectionBlockPos != null) {
                Direction thisConn = sameConnectionBlockPos.direction();
                return thisConn != null && thisConn.equals(direction);
            }
        }
        return false;
    }

    @Override
    public @Nullable IConnection getThisConnectionFrom(SimpleConnection connection) {
        if (connectionOne instanceof BezierConnection bezierConnection) {
            if (connection.isSameConnection(bezierConnection.getFromPos()))
                return bezierConnection;
        }
        if (connectionTwo instanceof BezierConnection bezierConnection) {
            if (connection.isSameConnection(bezierConnection.getFromPos()))
                return bezierConnection;
        }
        return null;
    }

    @Override
    public boolean hasConnectionAvailable() {
        return connectionTwo == null || connectionOne == null;
    }

    public boolean isConnected() {
        return connectionOne != null || connectionTwo != null;
    }

    @Override
    public void setConnection(IConnection connection, Direction thisConnectionDir) {
        if (connectionOne == null) {
            connectionOne = connection;
        } else if (connectionTwo == null) {
            connectionTwo = connection;
        } else {
            HypertubeMod.LOGGER.error(new TubeConnectionException("Connection could not define connection", connection, connectionOne, connectionTwo).getMessage());
            return;
        }
        if (level != null && !level.isClientSide()) {
            BlockState blockState = level.getBlockState(worldPosition);
            if (blockState.getBlock() instanceof HypertubeBlock hypertubeBlock) {
                hypertubeBlock.updateBlockStateFromEntity(blockState, level, worldPosition);
                if (thisConnectionDir != null) {
                    BlockState state = hypertubeBlock.getState(blockState, List.of(thisConnectionDir), true);
                    hypertubeBlock.updateBlockState(level, worldPosition, state);
                }
            }
        }
        setChanged();
        sync();
    }

    @Override
    public void clearConnection(IConnection connection) {
        if (connectionOne != null && connectionOne.isSameConnection(connection)) {
            connectionOne = null;
        } else if (connectionTwo != null && connectionTwo.isSameConnection(connection)) {
            connectionTwo = null;
        } else {
            HypertubeMod.LOGGER.error(new TubeConnectionException("Connection could not be cleared", connection, connectionOne, connectionTwo).getMessage());
            return;
        }
        setChanged();
        sync();
    }

    @Override
    public int blockBroken() {
        int toDrop = 0;
        if (connectionOne != null) {
            toDrop = blockBroken(level, connectionOne, worldPosition);
        }
        if (connectionTwo != null) {
            toDrop += blockBroken(level, connectionTwo, worldPosition);
        }
        return toDrop;
    }

    @Override
    public List<Direction> getFacesConnectable() {
        if (connectionOne != null && connectionTwo != null) return List.of();

        List<Direction> possibleDirections = ((HyperAcceleratorBlock) getBlockState().getBlock()).getConnectedFaces(getBlockState());

        possibleDirections.removeIf(direction -> {
            if (connectionOne != null) {
                return getConnectionDirection(direction, connectionOne);
            }
            if (connectionTwo != null) {
                return getConnectionDirection(direction, connectionTwo);
            }
            return false;
        });
        return possibleDirections;
    }
    @Override
    public List<IConnection> getConnections() {
        List<IConnection> connections = new ArrayList<>();
        if (connectionOne != null) {
            connections.add(connectionOne);
        }
        if (connectionTwo != null) {
            connections.add(connectionTwo);
        }
        return connections;
    }

    public void sync() {
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    public Vec3 getExitDirection() {
        if (connectionOne != null && connectionTwo != null) {
            return null;
        }
        if (connectionTwo != null) {
            return Vec3.atLowerCornerOf(IConnection.getSameConnectionBlockPos(connectionTwo, level, getBlockPos()).direction().getOpposite().getNormal());
        }
        if (connectionOne != null) {
            return Vec3.atLowerCornerOf(IConnection.getSameConnectionBlockPos(connectionOne, level, getBlockPos()).direction().getOpposite().getNormal());
        }
        return null;
    }
}
