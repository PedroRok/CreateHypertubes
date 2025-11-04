package com.pedrorok.hypertube.blocks.blockentities;

import com.pedrorok.hypertube.HypertubeMod;
import com.pedrorok.hypertube.blocks.HypertubeBlock;
import com.pedrorok.hypertube.core.connection.TubeConnectionException;
import com.pedrorok.hypertube.core.connection.interfaces.IConnection;
import com.pedrorok.hypertube.registry.ModBlockEntities;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

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

    public HypertubeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.HYPERTUBE.get(), pos, state);
    }

    // --------- Nbt Methods ---------
    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(compound, registries, clientPacket);

        // Limpa conexões antigas antes de ler
        IConnection oldConnectionOne = this.connectionOne;
        IConnection oldConnectionTwo = this.connectionTwo;

        this.connectionOne = null;
        this.connectionTwo = null;

        if (compound.contains("ConnectionTo")) {
            this.connectionOne = getConnection(compound, "ConnectionTo");
        }
        if (compound.contains("ConnectionFrom")) {
            this.connectionTwo = getConnection(compound, "ConnectionFrom");
        }

        // Se houve mudança, sincroniza (importante para cliente)
        boolean changed = !areConnectionsEqual(oldConnectionOne, this.connectionOne) ||
                          !areConnectionsEqual(oldConnectionTwo, this.connectionTwo);

        if (changed && clientPacket) {
            // Força atualização visual no cliente
            if (level != null) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    }

    @Override
    protected void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(compound, registries, clientPacket);
        writeConnection(compound,
                new Tuple<>(connectionOne, "ConnectionTo"),
                new Tuple<>(connectionTwo, "ConnectionFrom"));
    }
    // --------- Nbt Methods ---------

    /**
     * Compara duas conexões para verificar se são iguais
     */
    private boolean areConnectionsEqual(IConnection conn1, IConnection conn2) {
        if (conn1 == null && conn2 == null) return true;
        if (conn1 == null || conn2 == null) return false;
        return conn1.isSameConnection(conn2);
    }

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
            HypertubeMod.LOGGER.error(new TubeConnectionException(
                    "Connection could not define connection",
                    connection, connectionOne, connectionTwo).getMessage());
            return;
        }

        // Marca como alterado e sincroniza
        setChanged();

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

        // SEMPRE sincroniza, independente do level
        sync();
    }

    @Override
    public void clearConnection(IConnection connection) {
        boolean connectionCleared = false;

        if (connectionOne != null && connectionOne.isSameConnection(connection)) {
            connectionOne = null;
            connectionCleared = true;
        } else if (connectionTwo != null && connectionTwo.isSameConnection(connection)) {
            connectionTwo = null;
            connectionCleared = true;
        }

        if (!connectionCleared) {
            HypertubeMod.LOGGER.error(new TubeConnectionException(
                    "Connection could not be cleared",
                    connection, connectionOne, connectionTwo).getMessage());
            return;
        }

        // Marca como alterado e sincroniza
        setChanged();

        if (level != null && !level.isClientSide()) {
            BlockState blockState = level.getBlockState(worldPosition);
            if (blockState.getBlock() instanceof HypertubeBlock hypertubeBlock) {
                hypertubeBlock.updateBlockStateFromEntity(blockState, level, worldPosition);
            }
        }

        // SEMPRE sincroniza
        sync();
    }

    @Override
    protected int getConnectionCount() {
        return 2;
    }

    /**
     * Garante sincronização quando o chunk é carregado
     */
    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide()) {
            // Força sincronização inicial
            sync();
        }
    }
}