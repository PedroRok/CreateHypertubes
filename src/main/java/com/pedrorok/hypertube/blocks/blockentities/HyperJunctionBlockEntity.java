package com.pedrorok.hypertube.blocks.blockentities;

import com.pedrorok.hypertube.HypertubeMod;
import com.pedrorok.hypertube.blocks.HyperJunctionBlock;
import com.pedrorok.hypertube.blocks.HypertubeBlock;
import com.pedrorok.hypertube.blocks.blockentities.parent.ActionTubeBlockEntity;
import com.pedrorok.hypertube.config.ServerConfig;
import com.pedrorok.hypertube.core.connection.BezierConnection;
import com.pedrorok.hypertube.core.connection.TubeConnectionException;
import com.pedrorok.hypertube.core.connection.interfaces.IConnection;
import com.pedrorok.hypertube.core.data.JunctionMode;
import com.pedrorok.hypertube.core.sound.TubeSoundManager;
import com.pedrorok.hypertube.core.travel.TravelConstants;
import com.pedrorok.hypertube.utils.JunctionDirectionUtils;
import com.pedrorok.hypertube.utils.ModColors;
import com.pedrorok.hypertube.utils.TubePulseRenderer;
import com.simibubi.create.api.equipment.goggles.IHaveHoveringInformation;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
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
public class HyperJunctionBlockEntity extends ActionTubeBlockEntity implements IHaveHoveringInformation {

    private final UUID tubeSoundId = UUID.randomUUID();

    @Getter
    private IConnection connectionOne;
    @Getter
    private IConnection connectionTwo;
    @Getter
    private IConnection connectionThree;

    public HyperJunctionBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    // --------- Nbt Methods ---------
    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(compound, registries, clientPacket);
        if (compound.contains("ConnectionOne")) {
            connectionOne = getConnectionRelative(compound, "ConnectionOne", worldPosition);
        }
        if (compound.contains("ConnectionTwo")) {
            connectionTwo = getConnectionRelative(compound, "ConnectionTwo", worldPosition);
        }
        if (compound.contains("ConnectionThree")) {
            connectionThree = getConnectionRelative(compound, "ConnectionThree", worldPosition);
        }
    }

    @Override
    protected void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(compound, registries, clientPacket);
        writeConnectionRelative(compound, worldPosition,
                new Tuple<>(connectionOne, "ConnectionOne"),
                new Tuple<>(connectionTwo, "ConnectionTwo"),
                new Tuple<>(connectionThree, "ConnectionThree"));
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
    public void tick() {
        super.tick();
        if (level.isClientSide) {
            tickClient();
            return;
        }

        BlockState state = this.getBlockState();
        BlockPos pos = this.getBlockPos();

        float actualSpeed = Math.abs(this.getSpeed());
        Boolean isOpen = state.getValue(HyperJunctionBlock.OPEN);

        LivingEntity nearbyEntity = getNearbyLivingEntities((ServerLevel) level, pos.getCenter());

        boolean canOpen = nearbyEntity != null && nearbyEntity.getPersistentData().getBoolean(TravelConstants.TRAVEL_TAG);

        isTubeClosed(canOpen, isOpen);
    }


    @OnlyIn(Dist.CLIENT)
    private void tickClient() {
        float actualSpeed = Math.abs(this.getSpeed());
        TubeSoundManager.TubeAmbientSound sound = TubeSoundManager.getAmbientSound(tubeSoundId);
        if (actualSpeed < TravelConstants.NEEDED_SPEED) {
            sound.tickClientPlayerSounds();
            return;
        }
        playClientEffects(sound);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return false;
        if (mc.player.tickCount % 10 != 0) return false;
        if (mc.player.getPersistentData().getBoolean(TravelConstants.TRAVEL_TAG)) return false;

        List<Direction> connectedFaces = JunctionDirectionUtils.getConnectedFaces(getBlockState(), null, (HyperJunctionBlock) getBlockState().getBlock());
        renderFromDirections(connectedFaces, 0.3f, ModColors.GREEN, 0.72f);
        if (!getBlockState().getValue(HyperJunctionBlock.JUNCTION_MODE).equals(JunctionMode.AUTOMATIC)) {
            List<Direction> fromCenterDirection = JunctionDirectionUtils.getConnectedFaces(getBlockState(), getBlockState().getValue(HyperJunctionBlock.FACING), (HyperJunctionBlock) getBlockState().getBlock());
            IConnection connectionInDirection = getConnectionInDirection(getBlockState().getValue(HyperJunctionBlock.FACING));
            if (connectionInDirection != null) {
                BezierConnection thisEntranceConnection = connectionInDirection.getThisEntranceConnection(mc.level);
                if (thisEntranceConnection != null) {
                    boolean inverted = thisEntranceConnection.isInverted(getBlockPos());
                    BlockPos pos = thisEntranceConnection.getFromPos().pos();
                    TubePulseRenderer.start(pos, thisEntranceConnection, !inverted, 2, 0.1f, 0.2f, ModColors.ORANGE, 2, 8, true, 0.6f);
                }
            }
            renderFromDirections(fromCenterDirection, 0.2f, ModColors.ORANGE, 0.6f);
        }

        return false;
    }

    @OnlyIn(Dist.CLIENT)
    private void renderFromDirections(List<Direction> directions, float speed, int color, float radius) {
        Minecraft mc = Minecraft.getInstance();
        for (Direction direction : directions) {
            IConnection iConnection = getConnectionInDirection(direction);
            if (iConnection == null) continue;
            BezierConnection connection = iConnection.getThisEntranceConnection(mc.level);
            if (connection == null) continue;
            boolean inverted = connection.isInverted(getBlockPos());
            BlockPos pos = connection.getFromPos().pos();
            TubePulseRenderer.start(pos, connection, inverted, 2, 0.1f, speed, color, 0.2f, 2, false, radius);
        }
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
        if (connectionThree != null) {
            connections.add(connectionThree);
        }
        return connections;
    }

    @Override
    public void setConnection(IConnection connection, Direction thisConnectionDir) {
        if (connectionOne == null) {
            connectionOne = connection;
        } else if (connectionTwo == null) {
            connectionTwo = connection;
        } else if (connectionThree == null) {
            connectionThree = connection;
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
        } else if (connectionThree != null && connectionThree.isSameConnection(connection)) {
            connectionThree = null;
        } else {
            HypertubeMod.LOGGER.error(new TubeConnectionException("Connection could not be cleared", connection, connectionOne, connectionTwo).getMessage());
            return;
        }
        setChanged();
        sync();
    }

    @Override
    public Vec3 getExitDirection(@Nullable Direction connectionDirection) {
        if (getBlockState().hasProperty(HyperJunctionBlock.FACING)
                && connectionDirection != null
                && connectionDirection.getOpposite() == getBlockState().getValue(HyperJunctionBlock.FACING)) {
            Direction facing = getBlockState().getValue(HyperJunctionBlock.FACING);
            if (level == null) return Vec3.atLowerCornerOf(facing.getNormal());
            facing = level.getRandom().nextBoolean() ? facing.getClockWise() : facing.getCounterClockWise();
            return Vec3.atLowerCornerOf(facing.getNormal());
        }
        return connectionDirection != null ? Vec3.atLowerCornerOf(connectionDirection.getNormal()) : null;
    }

    @Override
    public float getConnectionOffsetOnDirection(Direction direction) {
        return 0.65f;
    }

    @Override
    protected int getConnectionCount() {
        return 3;
    }

    @Override
    public void onSpeedChanged(float previousSpeed) {
        level.setBlock(getBlockPos(), this.getBlockState().setValue(HyperJunctionBlock.ACTIVE, Math.abs(this.getSpeed()) >= TravelConstants.NEEDED_SPEED), 3);
    }

    @Override
    public void remove() {
        super.remove();
        if (level.isClientSide) {
            TubeSoundManager.TubeAmbientSound sound = TubeSoundManager.getAmbientSound(tubeSoundId);
            sound.stopSound();
        }
    }


    // --------- Stress Methods ---------
    public float calculateStressApplied() {
        float impact = (float) ServerConfig.get().STRESS_IMPACT_ACCELERATOR.getAsDouble();
        this.lastStressApplied = impact;
        return impact;
    }
}
