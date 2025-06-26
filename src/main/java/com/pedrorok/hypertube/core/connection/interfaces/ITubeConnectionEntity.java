package com.pedrorok.hypertube.core.connection.interfaces;

import com.pedrorok.hypertube.HypertubeMod;
import com.pedrorok.hypertube.core.connection.BezierConnection;
import com.pedrorok.hypertube.core.connection.SimpleConnection;
import com.pedrorok.hypertube.core.connection.TubeConnectionException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author Rok, Pedro Lucas nmm. Created on 24/06/2025
 * @project Create Hypertube
 */
public interface ITubeConnectionEntity {


    default IConnection getConnection(CompoundTag tag, String key) {
        try {
            return BezierConnection.CODEC.parse(NbtOps.INSTANCE, tag.get(key))
                    .getOrThrow();
        } catch (Exception ignored) {
            return SimpleConnection.CODEC.parse(NbtOps.INSTANCE, tag.get(key))
                    .getOrThrow();
        }
    }

    default void writeConnection(CompoundTag tag, Tuple<@Nullable IConnection, String>... connections) {
        for (Tuple<@Nullable IConnection, String> connection : connections) {
            if (connection.getA() == null) continue;
            writeConnection(tag, connection.getA(), connection.getB());
        }
    }

    private void writeConnection(CompoundTag tag, IConnection connection, String key) {
        if (connection instanceof SimpleConnection simpleConnection) {
            tag.put(key, SimpleConnection.CODEC.encodeStart(NbtOps.INSTANCE, simpleConnection)
                    .getOrThrow());
        } else {
            tag.put(key, BezierConnection.CODEC.encodeStart(NbtOps.INSTANCE, (BezierConnection) connection)
                    .getOrThrow());
        }
    }

    // isPrimary | Connection
    @Nullable
    IConnection getConnectionInDirection(Direction direction);

    @Nullable
    IConnection getThisConnectionFrom(SimpleConnection connection);

    boolean hasConnectionAvailable();

    boolean isConnected();

    void setConnection(IConnection connection, Direction thisConnectionDir);

    void clearConnection(IConnection connection);

    /**
     * @return the number of blocks broken by this connection
     */
    int blockBroken();

    default int blockBroken(Level level, IConnection connection, BlockPos selfPos) {
        int toDrop = 0;
        BezierConnection thisEntranceConnection = connection.getThisEntranceConnection(level);
        if (thisEntranceConnection != null) {
            toDrop += (int) thisEntranceConnection.distance();
        }

        IConnection connectionToClear = null;
        BlockPos otherBlockPos = null;
        if (connection instanceof BezierConnection bezier) {
            SimpleConnection connectionTo = bezier.getToPos();
            SimpleConnection connectionFrom = bezier.getFromPos();
            if (connectionTo.pos().equals(selfPos)) {
                connectionToClear = connection;
                otherBlockPos = connectionFrom.pos();
            } else if (connectionFrom.pos().equals(selfPos)) {
                connectionToClear = connectionFrom;
                otherBlockPos = connectionTo.pos();
            }
        } else if (connection instanceof SimpleConnection simple) {
            if (simple.pos().equals(selfPos)) {
                try {
                    throw new TubeConnectionException("SimpleConnection should not be used here, use BezierConnection instead.", connection);
                } catch (TubeConnectionException e) {
                    HypertubeMod.LOGGER.error(e.getMessage());
                    return 0;
                }
            } else {
                otherBlockPos = simple.pos();
                connectionToClear = thisEntranceConnection;
            }
        }



        if (connectionToClear != null) {
            BlockEntity otherBlock = level.getBlockEntity(otherBlockPos);
            if (otherBlock instanceof ITubeConnectionEntity tubeConnection) {
                tubeConnection.clearConnection(connectionToClear);
            }
        }
        return toDrop;
    }

    List<Direction> getFacesConnectable();

    List<IConnection> getConnections();
}
