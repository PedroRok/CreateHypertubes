package com.pedrorok.hypertube.core.connection;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.pedrorok.hypertube.core.connection.interfaces.IConnection;
import com.pedrorok.hypertube.core.connection.interfaces.ITubeConnectionEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public record SimpleConnection(BlockPos pos, Direction direction, float offset) implements IConnection {

    @Deprecated
    public SimpleConnection(BlockPos pos, Direction direction) {
        this(pos, direction, 0f);
    }

    public static final Codec<SimpleConnection> CODEC = RecordCodecBuilder.create(i -> i.group(
            BlockPos.CODEC.fieldOf("pos").forGetter(SimpleConnection::pos),
            Direction.CODEC.fieldOf("direction").forGetter(SimpleConnection::direction),
            Codec.FLOAT.optionalFieldOf("offset", 0f).forGetter(SimpleConnection::offset)
    ).apply(i, SimpleConnection::new));

    public static final StreamCodec<ByteBuf, SimpleConnection> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, SimpleConnection::pos,
            Direction.STREAM_CODEC, SimpleConnection::direction,
            ByteBufCodecs.FLOAT, SimpleConnection::offset,
            SimpleConnection::new
    );

    public Vec3 getOffsetCenter() {
        Vec3 center = pos.getCenter();
        if (offset == 0f) return center;
        return center.add(
                direction.getStepX() * offset,
                direction.getStepY() * offset,
                direction.getStepZ() * offset
        );
    }

    @Override
    @Nullable
    public BezierConnection getThisEntranceConnection(Level level) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof ITubeConnectionEntity connection)) {
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
        if (connection instanceof SimpleConnection other && other.pos().equals(this.pos) && other.direction() == this.direction) return true;
        return connection instanceof BezierConnection bezier && bezier.getFromPos().equals(this);
    }

    @Override
    public SimpleConnection getThisConnection() {
        return this;
    }

    @Override
    public void updateTubeSegments(Level level) {
        BezierConnection thisEntranceConnection = getThisEntranceConnection(level);
        if (thisEntranceConnection != null) {
            thisEntranceConnection.updateTubeSegments(level);
        }
    }

    public BlockPos pos() {
        return pos;
    }

    public Direction direction() {
        return direction;
    }

    public float offset() {
        return offset;
    }
}