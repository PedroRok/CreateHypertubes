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
import net.minecraft.world.phys.Vec3;
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
                    .get().orThrow();
        } catch (Exception ignored) {
            return SimpleConnection.CODEC.parse(NbtOps.INSTANCE, tag.get(key))
                    .get().orThrow();
        }
    }

    default void writeConnection(CompoundTag tag, Tuple<@Nullable IConnection, String>... connections) {
        for (Tuple<@Nullable IConnection, String> connection : connections) {
            writeConnection(tag, connection.getA(), connection.getB());
        }
    }

    private void writeConnection(CompoundTag tag, IConnection connection, String key) {
        if (connection instanceof SimpleConnection simpleConnection) {
            tag.put(key, SimpleConnection.CODEC.encodeStart(NbtOps.INSTANCE, simpleConnection)
                    .get().orThrow());
        } else {
            tag.put(key, BezierConnection.CODEC.encodeStart(NbtOps.INSTANCE, (BezierConnection) connection)
                    .get().orThrow());
        }
    }

    default IConnection getConnectionRelative(CompoundTag tag, String key, BlockPos referencePos) {
        boolean isNewFormat = tag.contains(key + "_version");

        try {
            BezierConnection connection = BezierConnection.CODEC.parse(NbtOps.INSTANCE, tag.get(key))
                    .getOrThrow();

            if (isNewFormat) {
                SimpleConnection fromAbsolute = new SimpleConnection(
                        connection.getFromPos().pos().offset(referencePos),
                        connection.getFromPos().direction()
                );
                SimpleConnection toAbsolute = connection.getToPos() != null ? new SimpleConnection(
                        connection.getToPos().pos().offset(referencePos),
                        connection.getToPos().direction()
                ) : null;
                return new BezierConnection(fromAbsolute, toAbsolute, connection.getTubeSegments(), connection.getCachedRelativeBezierPoints());
            }
            return connection;
        } catch (Exception ignored) {
            try {
                SimpleConnection connection = SimpleConnection.CODEC.parse(NbtOps.INSTANCE, tag.get(key))
                        .getOrThrow();

                if (isNewFormat) {
                    return new SimpleConnection(
                            connection.pos().offset(referencePos),
                            connection.direction()
                    );
                }
                return connection;
            } catch (Exception e) {
                return null;
            }
        }
    }

    default void writeConnectionRelative(CompoundTag tag, BlockPos referencePos, Tuple<IConnection, String>... connections) {
        for (Tuple<IConnection, String> connection : connections) {
            writeConnectionRelativeSingle(tag, referencePos, connection.getA(), connection.getB());
            tag.putInt(connection.getB() + "_version", 1);
        }
    }

    default void writeConnectionRelativeSingle(CompoundTag tag, BlockPos referencePos, IConnection connection, String key) {
        if (connection instanceof SimpleConnection(BlockPos pos, Direction direction)) {
            // Convert absolute position to relative
            SimpleConnection relative = new SimpleConnection(
                    pos.subtract(referencePos),
                    direction
            );
            tag.put(key, SimpleConnection.CODEC.encodeStart(NbtOps.INSTANCE, relative)
                    .getOrThrow());
        } else if (connection instanceof BezierConnection bezierConnection) {
            // Convert absolute positions to relative
            SimpleConnection fromRelative = new SimpleConnection(
                    bezierConnection.getFromPos().pos().subtract(referencePos),
                    bezierConnection.getFromPos().direction()
            );
            SimpleConnection toRelative = bezierConnection.getToPos() != null ? new SimpleConnection(
                    bezierConnection.getToPos().pos().subtract(referencePos),
                    bezierConnection.getToPos().direction()
            ) : null;
            BezierConnection relative = new BezierConnection(fromRelative, toRelative, bezierConnection.getTubeSegments(), bezierConnection.getCachedRelativeBezierPoints());
            tag.put(key, BezierConnection.CODEC.encodeStart(NbtOps.INSTANCE, relative)
                    .getOrThrow());
        }
    }

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

    boolean wrenchClicked(Direction direction);

    @Nullable
    Vec3 getExitDirection();
}
