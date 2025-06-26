package com.pedrorok.hypertube.core.connection;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.pedrorok.hypertube.core.connection.interfaces.IConnection;
import com.pedrorok.hypertube.core.connection.interfaces.TubeConnectionEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public record SimpleConnection(BlockPos pos, Direction direction) implements IConnection {
    public static final Codec<SimpleConnection> CODEC = RecordCodecBuilder.create(i -> i.group(
            BlockPos.CODEC.fieldOf("pos").forGetter(SimpleConnection::pos),
            Direction.CODEC.fieldOf("direction").forGetter(SimpleConnection::direction)
    ).apply(i, SimpleConnection::new));

    public static final StreamCodec<ByteBuf, SimpleConnection> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, SimpleConnection::pos,
            Direction.STREAM_CODEC, SimpleConnection::direction,
            SimpleConnection::new
    );
    @Override
    @Nullable
    public BezierConnection getThisEntranceConnection(Level level) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof TubeConnectionEntity connection)) {
            return null;
        }
        IConnection thisConnection = connection.getThisConnectionFrom(this);
        if (thisConnection == null) return null;
        return thisConnection.getThisEntranceConnection(level);
    }

    @Override
    public Direction getThisEntranceDirection(Level level) {
        BezierConnection thisEntranceConnection = getThisEntranceConnection(level);
        if (thisEntranceConnection == null) return null;
        return thisEntranceConnection.getThisEntranceDirection(level);
    }

    @Override
    public boolean isSameConnection(IConnection connection) {
        if (connection instanceof SimpleConnection && connection.equals(this)) return true;
        return connection instanceof BezierConnection bezier && bezier.getFromPos().equals(this);
    }

    @Override
    public SimpleConnection getThisConnection() {
        return this;
    }

    public BlockPos pos() {
        return pos;
    }

    public Direction direction() {
        return direction;
    }
}
