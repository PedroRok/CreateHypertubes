package com.pedrorok.hypertube.core.connection;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.pedrorok.hypertube.core.connection.interfaces.IBezierConnection;
import com.pedrorok.hypertube.core.connection.interfaces.IConnection;
import com.pedrorok.hypertube.utils.CodecUtils;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author Rok, Pedro Lucas nmm. Created on 24/04/2025
 * @project Create Hypertube
 */
public class BezierConnection extends IBezierConnection<SimpleConnection> implements IConnection {

    public static final Codec<BezierConnection> CODEC = RecordCodecBuilder.create(i -> i.group(
            SimpleConnection.CODEC.fieldOf("fromPos").forGetter(BezierConnection::getFromPos),
            SimpleConnection.CODEC.fieldOf("toPos").forGetter(BezierConnection::getToPos),
            Codec.INT.fieldOf("tubeSegments").forGetter(BezierConnection::getTubeSegments),
            Vec3.CODEC.listOf().fieldOf("curvePoints").forGetter((connection) -> connection.getBezierPoints(false))
    ).apply(i, BezierConnection::new));

    public static final StreamCodec<ByteBuf, BezierConnection> STREAM_CODEC = StreamCodec.composite(
            SimpleConnection.STREAM_CODEC, BezierConnection::getFromPos,
            SimpleConnection.STREAM_CODEC, BezierConnection::getToPos,
            CodecUtils.INTEGER, BezierConnection::getTubeSegments,
            CodecUtils.VEC3_LIST, (connection) -> connection.getBezierPoints(false),
            BezierConnection::new
    );

    @Getter
    private int tubeSegments;

    private BezierConnection(SimpleConnection fromPos, SimpleConnection toPos, int tubeSegments, List<Vec3> bezierPoints) {
        this(fromPos, toPos, tubeSegments, (int) Math.max(3, fromPos.pos().getCenter().distanceTo(toPos.pos().getCenter())));
        this.setBezierPoints(bezierPoints);
    }

    public BezierConnection(SimpleConnection fromPos, @Nullable SimpleConnection toPos) {
        this(fromPos, toPos, 1, toPos != null ? (int) Math.max(3, fromPos.pos().getCenter().distanceTo(toPos.pos().getCenter())) : 0);
    }

    public BezierConnection(SimpleConnection fromPos, SimpleConnection toPos, int tubeSegments, int detailLevel) {
        super(fromPos, toPos, detailLevel);
        this.tubeSegments = tubeSegments;
    }

    public static BezierConnection of(SimpleConnection from, @Nullable SimpleConnection toPos) {
        return new BezierConnection(from, toPos);
    }

    @Override
    public BezierConnection getThisEntranceConnection(Level level) {
        return this;
    }

    @Override
    public Direction getThisEntranceDirection(Level level) {
        return getFromPos().direction();
    }

    @Override
    public boolean isSameConnection(IConnection connection) {
        return getFromPos().isSameConnection(connection) || connection.equals(this);
    }

    @Override
    public SimpleConnection getThisConnection() {
        return getFromPos();
    }

    @Override
    public void updateTubeSegments(Level level) {
        tubeSegments = tubeSegments == 1 ? 2 : 1;
    }

    @Override
    public Vec3 getFromPosCenter() {
        return getFromPos().pos().getCenter();
    }

    @Override
    public Vec3 getToPosCenter() {
        return getToPos().pos().getCenter();
    }
}
