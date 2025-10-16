package com.pedrorok.hypertube.blocks.blockentities;

import com.pedrorok.hypertube.core.connection.BezierConnection;
import com.pedrorok.hypertube.core.connection.SimpleConnection;
import com.pedrorok.hypertube.core.connection.interfaces.IConnection;
import com.pedrorok.hypertube.core.connection.interfaces.ITubeConnection;
import com.pedrorok.hypertube.core.connection.interfaces.ITubeConnectionEntity;
import com.simibubi.create.api.event.BlockEntityBehaviourEvent;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author Rok, Pedro Lucas nmm. Created on 11/08/2025
 * @project Create Hypertube
 */
public abstract class TubeBlockEntity extends BlockEntity implements ITubeConnectionEntity {
    public TubeBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    protected abstract int getConnectionCount();

    public void sync() {
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    public boolean getConnectionDirection(Direction direction, IConnection connection) {
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
    public List<Direction> getFacesConnectable() {
        List<IConnection> connections = getConnections();
        if (connections.size() >= getConnectionCount()) return List.of();

        List<Direction> possibleDirections = ((ITubeConnection) getBlockState().getBlock()).getConnectedFaces(getBlockState());
        if (possibleDirections.isEmpty()) {
            return List.of(Direction.values());
        }

        possibleDirections.removeIf(direction -> {
            for (IConnection connection : connections) {
                return getConnectionDirection(direction, connection);
            }
            return false;
        });
        return possibleDirections;
    }

    @Override
    public @Nullable IConnection getConnectionInDirection(Direction direction) {
        List<IConnection> connections = getConnections();
        if (connections.isEmpty()) return null;
        for (IConnection connection : connections) {
            if (getConnectionDirection(direction, connection)) {
                return connection;
            }
        }
        return null;
    }

    @Override
    public @Nullable IConnection getThisConnectionFrom(SimpleConnection connection) {
        List<IConnection> connections = getConnections();
        if (connections.isEmpty()) return null;
        for (IConnection conn : connections) {
            if (!(conn instanceof BezierConnection bezierConn)) continue;
            if (connection.isSameConnection(bezierConn.getFromPos())) {
                return conn;
            }
        }
        return null;
    }

    @Override
    public boolean hasConnectionAvailable() {
        List<IConnection> connections = getConnections();
        if (connections.isEmpty()) return true;
        return connections.size() < getConnectionCount();
    }

    public boolean isConnected() {
        List<IConnection> connections = getConnections();
        return !connections.isEmpty();
    }

    @Override
    public int blockBroken() {
        int toDrop = 0;
        List<IConnection> connections = getConnections();
        if (connections.isEmpty()) return toDrop;
        for (IConnection connection : connections) {
            toDrop += blockBroken(level, connection, worldPosition);
        }
        return toDrop;
    }

    @Override
    public Vec3 getExitDirection() {
        List<IConnection> connections = getConnections();
        if (connections.isEmpty()) return null;
        IConnection first = connections.get(0);
        return Vec3.atLowerCornerOf(IConnection.getSameConnectionBlockPos(first, level, getBlockPos()).direction().getOpposite().getNormal());
    }

    public float getSpeed() {
        return 16;
    }

    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.saveAdditional(tag, registries);
    }


    /**
     * Hook only these in future subclasses of STE
     */
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.loadAdditional(tag, registries);
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        read(tag, registries, false);
    }
}
