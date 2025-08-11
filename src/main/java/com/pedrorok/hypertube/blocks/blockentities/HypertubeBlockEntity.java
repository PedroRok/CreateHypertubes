package com.pedrorok.hypertube.blocks.blockentities;

import com.pedrorok.hypertube.HypertubeMod;
import com.pedrorok.hypertube.blocks.HypertubeBlock;
import com.pedrorok.hypertube.core.connection.TubeConnectionException;
import com.pedrorok.hypertube.core.connection.interfaces.IConnection;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Rok, Pedro Lucas nmm. Created on 24/04/2025
 * @project Create Hypertube
 */
@Getter
public class HypertubeBlockEntity extends TubeBlockEntity {

    private IConnection connectionOne;
    private IConnection connectionTwo;

    public HypertubeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    // --------- Nbt Methods ---------
    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(compound, registries, clientPacket);
        if (compound.contains("ConnectionTo")) {
            this.connectionOne = getConnection(compound, "ConnectionTo");
        }
        if (compound.contains("ConnectionFrom")) {
            this.connectionTwo = getConnection(compound, "ConnectionFrom");
        }
    }

    @Override
    protected void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(compound, registries, clientPacket);
        writeConnection(compound, new Tuple<>(connectionOne, "ConnectionTo"), new Tuple<>(connectionTwo, "ConnectionFrom"));
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

        if (level != null && !level.isClientSide()) {
            BlockState blockState = level.getBlockState(worldPosition);
            if (blockState.getBlock() instanceof HypertubeBlock hypertubeBlock) {
                hypertubeBlock.updateBlockStateFromEntity(blockState, level, worldPosition);
            }
        }
        setChanged();
        sync();
    }

    @Override
    protected int getConnectionCount() {
        return 2;
    }
}