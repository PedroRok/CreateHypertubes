package com.pedrorok.hypertube.blocks.blockentities;

import com.pedrorok.hypertube.HypertubeMod;
import com.pedrorok.hypertube.blocks.HyperEntranceBlock;
import com.pedrorok.hypertube.config.ServerConfig;
import com.pedrorok.hypertube.core.connection.TubeConnectionException;
import com.pedrorok.hypertube.core.connection.interfaces.IConnection;
import com.pedrorok.hypertube.core.sound.TubeSoundManager;
import com.pedrorok.hypertube.core.travel.TravelConstants;
import com.pedrorok.hypertube.core.travel.TravelManager;
import com.pedrorok.hypertube.registry.ModBlockEntities;
import com.pedrorok.hypertube.utils.TubeUtils;
import lombok.Getter;
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

import java.util.ArrayList;
import java.util.List;

/**
 * @author Rok, Pedro Lucas nmm. Created on 21/04/2025
 * @project Create Hypertube
 */
public class HyperEntranceBlockEntity extends ActionTubeBlockEntity {


    @Getter
    private IConnection connection;

    public HyperEntranceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.HYPERTUBE_ENTRANCE.get(), pos, state);
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

    public void tick() {
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
        if (actualSpeed < TravelConstants.NEEDED_SPEED) {
            if (isOpen) {
                level.setBlock(pos, state.setValue(HyperEntranceBlock.OPEN, false), 3);
                playOpenCloseSound(false);
            }
            return;
        }

        boolean isLocked = !getBlockState().getValue(HyperEntranceBlock.LOCKED);
        LivingEntity nearbyEntity = getNearbyLivingEntities((ServerLevel) level, pos.getCenter());

        boolean canOpen = nearbyEntity != null && (isLocked || nearbyEntity.isShiftKeyDown() || nearbyEntity.getPersistentData().getBoolean(TravelConstants.TRAVEL_TAG));


        if (isTubeClosed(canOpen, isOpen)) return;

        LivingEntity inRangeEntity = getInRangeLivingEntities((ServerLevel) level, pos.getCenter(), state.getValue(HyperEntranceBlock.FACING));
        if (inRangeEntity == null) return;

        if (!isLocked && !inRangeEntity.isShiftKeyDown() && !inRangeEntity.getPersistentData().getBoolean(TravelConstants.TRAVEL_TAG)) {
            return;
        }

        TravelManager.tryStartTravel(inRangeEntity, pos, state, TubeUtils.calculateTravelSpeed(actualSpeed));
    }

    @OnlyIn(Dist.CLIENT)
    private void tickClient(boolean isBlocked) {
        float actualSpeed = Math.abs(this.getSpeed());
        TubeSoundManager.TubeAmbientSound sound = TubeSoundManager.getAmbientSound(tubeSoundId);
        if (actualSpeed < TravelConstants.NEEDED_SPEED || isBlocked) {
            sound.tickClientPlayerSounds();
            return;
        }
        playClientEffects(sound);
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
    public List<Direction> getFacesConnectable() {
        if (connection != null) return List.of();
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

    @Override
    public Vec3 getExitDirection() {
        if (getBlockState().hasProperty(HyperEntranceBlock.FACING)) {
            Direction facing = getBlockState().getValue(HyperEntranceBlock.FACING).getOpposite();
            return Vec3.atLowerCornerOf(facing.getNormal());
        }
        return null;
    }

    @Override
    protected int getConnectionCount() {
        return 1;
    }
}
