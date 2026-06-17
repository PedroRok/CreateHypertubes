package com.pedrorok.hypertube.core.connection;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.pedrorok.hypertube.core.connection.interfaces.IConnection;
import com.pedrorok.hypertube.core.placement.ResponseDTO;
import com.pedrorok.hypertube.utils.CodecUtils;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.data.Pair;
import net.createmod.catnip.outliner.Outliner;
import net.createmod.catnip.theme.Color;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author Rok, Pedro Lucas nmm. Created on 24/04/2025
 * @project Create Hypertube
 */
public class BezierConnection implements IConnection {

    public static final Codec<BezierConnection> CODEC = RecordCodecBuilder.create(i -> i.group(
            SimpleConnection.CODEC.fieldOf("fromPos").forGetter(BezierConnection::getFromPos),
            SimpleConnection.CODEC.fieldOf("toPos").forGetter(BezierConnection::getToPos),
            Codec.INT.fieldOf("tubeSegments").forGetter(BezierConnection::getTubeSegments),
            Vec3.CODEC.listOf().fieldOf("curvePoints").forGetter(BezierConnection::getCachedRelativeBezierPoints)
    ).apply(i, BezierConnection::new));

    public static final StreamCodec<ByteBuf, BezierConnection> STREAM_CODEC = StreamCodec.composite(
            SimpleConnection.STREAM_CODEC, BezierConnection::getFromPos,
            SimpleConnection.STREAM_CODEC, BezierConnection::getToPos,
            CodecUtils.INTEGER, BezierConnection::getTubeSegments,
            CodecUtils.VEC3_LIST, BezierConnection::getCachedRelativeBezierPoints,
            BezierConnection::new
    );


    private final static float MAX_REASONABLE_DISTANCE = 1000F;
    public static final float MAX_DISTANCE = 40.0f;
    public static final float MAX_ANGLE = 0.6f;

    @Getter
    private final UUID uuid = UUID.randomUUID();
    @Getter
    private final SimpleConnection fromPos;
    @Getter
    private @Nullable SimpleConnection toPos;

    @Getter
    private int tubeSegments;

    @Getter
    private final List<Vec3> cachedRelativeBezierPoints;

    private ResponseDTO valid;
    private final int detailLevel;

    public BezierConnection(SimpleConnection fromPos, SimpleConnection toPos, int tubeSegments, List<Vec3> cachedPoints) {
        this.fromPos = fromPos;
        this.toPos = toPos;
        this.tubeSegments = tubeSegments;
        this.detailLevel = toPos != null ? (int) Math.max(3, fromPos.pos().getCenter().distanceTo(toPos.pos().getCenter())) : 0;
        this.cachedRelativeBezierPoints = cachedPoints != null ? cachedPoints : calculateRelativeBezierPoints();
    }

    public BezierConnection(SimpleConnection fromPos, @Nullable SimpleConnection toPos) {
        this(fromPos, toPos, 1, toPos != null ? (int) Math.max(3, fromPos.pos().getCenter().distanceTo(toPos.pos().getCenter())) : 0);
    }

    public BezierConnection(SimpleConnection fromPos, @Nullable SimpleConnection toPos, int tubeSegments, int detailLevel) {
        this.fromPos = fromPos;
        this.toPos = toPos;
        this.detailLevel = detailLevel;
        this.tubeSegments = tubeSegments;
        this.cachedRelativeBezierPoints = calculateRelativeBezierPoints();
    }

    private List<Vec3> calculateRelativeBezierPoints() {
        if (toPos == null) return List.of();
        if (distance() >= MAX_REASONABLE_DISTANCE) return List.of();

        Vec3 fromAbsolute = fromPos.getOffsetCenter();
        Vec3 toAbsolute = toPos.getOffsetCenter();

        Vec3 originAbsolute = Vec3.atLowerCornerOf(fromPos.pos());
        Vec3 fromRelative = fromAbsolute.subtract(originAbsolute);
        Vec3 toRelative = toAbsolute.subtract(originAbsolute);

        double distance = fromRelative.distanceTo(toRelative);
        Vec3 controlPoint1 = createFirstControlPoint(fromRelative, fromPos.direction(), distance);
        Vec3 controlPoint2 = createSecondControlPoint(toRelative, fromPos.direction(), distance,
                toPos.direction() != null ? Vec3.atLowerCornerOf(toPos.direction().getNormal()) : null);

        List<Vec3> curvePoints = new ArrayList<>();
        for (int i = 0; i <= detailLevel; i++) {
            double t = (double) i / detailLevel;
            Vec3 point = cubicBezier(fromRelative, controlPoint1, controlPoint2, toRelative, t);
            curvePoints.add(point);
        }

        return curvePoints;
    }

    @Deprecated
    public List<Vec3> getBezierPoints() {
        if (cachedRelativeBezierPoints.isEmpty()) return List.of();

        Vec3 originAbsolute = Vec3.atLowerCornerOf(fromPos.pos());
        List<Vec3> absolutePoints = new ArrayList<>(cachedRelativeBezierPoints.size());
        for (Vec3 relativePoint : cachedRelativeBezierPoints) {
            absolutePoints.add(originAbsolute.add(relativePoint));
        }
        return absolutePoints;
    }

    public List<Vec3> getBezierPoints(Level level, BlockPos currentFromPos) {
        if (cachedRelativeBezierPoints.isEmpty()) return List.of();

        Vec3 originAbsolute = Vec3.atLowerCornerOf(currentFromPos);
        List<Vec3> absolutePoints = new ArrayList<>(cachedRelativeBezierPoints.size());
        for (Vec3 relativePoint : cachedRelativeBezierPoints) {
            absolutePoints.add(originAbsolute.add(relativePoint));
        }
        return absolutePoints;
    }

    public List<Vec3> getRelativeBezierPoints(BlockPos originPos) {
        if (cachedRelativeBezierPoints.isEmpty()) return List.of();

        if (originPos.equals(fromPos.pos())) {
            return new ArrayList<>(cachedRelativeBezierPoints);
        }

        BlockPos offset = fromPos.pos().subtract(originPos);
        Vec3 offsetVec = new Vec3(offset.getX(), offset.getY(), offset.getZ());
        List<Vec3> adjustedPoints = new ArrayList<>(cachedRelativeBezierPoints.size());
        for (Vec3 point : cachedRelativeBezierPoints) {
            adjustedPoints.add(point.add(offsetVec));
        }
        return adjustedPoints;
    }

    public List<Vec3> getRelativeBezierPoints(Level level, BlockPos originPos) {
        return new ArrayList<>(cachedRelativeBezierPoints);
    }

    private Vec3 createFirstControlPoint(Vec3 from, Direction direction, double distance) {
        double controlDistance = distance * 0.4;
        return from.add(
                direction.getStepX() * controlDistance,
                direction.getStepY() * controlDistance,
                direction.getStepZ() * controlDistance
        );
    }

    private Vec3 createSecondControlPoint(Vec3 to, Direction fromDirection, double distance, @Nullable Vec3 finalDirection) {
        if (finalDirection != null) {
            double controlDistance = distance * 0.4;
            return to.subtract(
                    finalDirection.x * controlDistance,
                    finalDirection.y * controlDistance,
                    finalDirection.z * controlDistance
            );
        } else {
            Direction oppositeDirection = fromDirection.getOpposite();
            double controlDistance = distance * 0.4;
            return to.add(
                    oppositeDirection.getStepX() * controlDistance,
                    oppositeDirection.getStepY() * controlDistance,
                    oppositeDirection.getStepZ() * controlDistance
            );
        }
    }

    private Vec3 cubicBezier(Vec3 p0, Vec3 p1, Vec3 p2, Vec3 p3, double t) {
        // B(t) = (1-t)^3 * P0 + 3(1-t)^2 * t * P1 + 3(1-t) * t^2 * P2 + t^3 * P3
        double oneMinusT = 1 - t;
        double oneMinusTCubed = oneMinusT * oneMinusT * oneMinusT;
        double oneMinusTSquared = oneMinusT * oneMinusT;
        double tSquared = t * t;
        double tCubed = tSquared * t;

        double x = oneMinusTCubed * p0.x + 3 * oneMinusTSquared * t * p1.x + 3 * oneMinusT * tSquared * p2.x + tCubed * p3.x;
        double y = oneMinusTCubed * p0.y + 3 * oneMinusTSquared * t * p1.y + 3 * oneMinusT * tSquared * p2.y + tCubed * p3.y;
        double z = oneMinusTCubed * p0.z + 3 * oneMinusTSquared * t * p1.z + 3 * oneMinusT * tSquared * p2.z + tCubed * p3.z;

        return new Vec3(x, y, z);
    }

    public float getMaxAngleBezierAngle() {
        List<Vec3> points = getBezierPoints();

        if (distance() > MAX_REASONABLE_DISTANCE) return 0;

        Vec3 first = points.getFirst();
        Vec3 second = points.get(1);
        Direction direction = fromPos.direction();
        Vec3 firstDirection = new Vec3(direction.getStepX(), direction.getStepY(), direction.getStepZ());
        Vec3 secondDirection = second.subtract(first).normalize();
        float initialAngle = (float) Math.acos(firstDirection.dot(secondDirection) / (firstDirection.length() * secondDirection.length()));
        if (initialAngle >= 2.) {
            return initialAngle;
        }

        return getMaxAngle(points);
    }

    private float getMaxAngle(List<Vec3> points) {
        float maxAngle = 0;
        Vec3 lastPoint = points.getFirst();
        for (int i = 1; i < points.size() - 1; i++) {
            Vec3 currentPoint = points.get(i);
            Vec3 nextPoint = points.get(i + 1);

            Vec3 vector1 = currentPoint.subtract(lastPoint);
            Vec3 vector2 = nextPoint.subtract(currentPoint);
            float angle = (float) Math.acos(vector1.dot(vector2) / (vector1.length() * vector2.length()));
            maxAngle = Math.max(maxAngle, angle);
            lastPoint = currentPoint;
        }
        return maxAngle;
    }

    public float distance() {
        if (toPos == null) return 0;
        return (float) fromPos.pos().getCenter().distanceTo(toPos.pos().getCenter());
    }


    public ResponseDTO getValidation() {
        if (valid != null) return valid;
        if (fromPos == null || toPos == null) {
            valid = ResponseDTO.invalid("placement.create_hypertube.no_valid_points");
            return valid;
        }
        if (getMaxAngleBezierAngle() >= MAX_ANGLE) {
            valid = ResponseDTO.invalid("placement.create_hypertube.angle_too_high");
            return valid;
        }
        if (distance() >= MAX_DISTANCE) {
            valid = ResponseDTO.invalid("placement.create_hypertube.distance_too_high");
            return valid;
        }
        if (distance() <= 1) {
            valid = ResponseDTO.invalid();
            return valid;
        }

        return ResponseDTO.get(true);
    }

    public static BezierConnection of(SimpleConnection from, @Nullable SimpleConnection toPos) {
        return new BezierConnection(from, toPos);
    }


    @OnlyIn(Dist.CLIENT)
    public void drawPath(LerpedFloat animation, boolean isValid) {
        if (distance() > MAX_REASONABLE_DISTANCE) return;

        List<Vec3> points = getBezierPoints();
        if (points.isEmpty()) return;

        Vec3 pos1 = points.getFirst();
        int id = 0;
        for (int i = 1; i < points.size(); i++) {
            Vec3 bezierPoint = points.get(i);
            line(uuid, id, pos1, bezierPoint, animation, !isValid);
            pos1 = bezierPoint;
            id++;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void line(UUID uuid, int id, Vec3 start, Vec3 end, LerpedFloat animation, boolean hasException) {
        int color = Color.mixColors(0xEA5C2B, 0x95CD41, animation.getValue());
        if (hasException) {
            Vec3 diff = end.subtract(start);
            start = start.add(diff.scale(0.2));
            end = start.add(diff.scale(-0.2));
        }
        Outliner.getInstance().showLine(Pair.of(uuid, id), start, end)
                .lineWidth(1 / 8f)
                .disableLineNormals()
                .colored(color);
    }

    @OnlyIn(Dist.CLIENT)
    public static void outlineBlocks(BlockPos pos) {
        Outliner.getInstance().showAABB(pos.asLong(), new AABB(pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1,
                        pos.getX(), pos.getY(), pos.getZ()))
                .colored(0xEA5C2B)
                .lineWidth(1 / 8f)
                .disableLineNormals();
    }

    // check if the referencePos is the same as this bezierconnection origin
    public boolean isInverted(BlockPos refencePos) {
        return toPos.pos().equals(refencePos);
    }


    public BezierConnection invert() {
        return new BezierConnection(
                new SimpleConnection(toPos.pos(), toPos.direction().getOpposite(), toPos.offset()),
                new SimpleConnection(fromPos.pos(), fromPos.direction().getOpposite(), fromPos.offset()),
                tubeSegments,
                detailLevel
        );
    }

    @Override
    public BezierConnection getThisEntranceConnection(Level level) {
        return this;
    }

    @Override
    public Direction getThisEntranceDirection(Level level) {
        return fromPos.direction();
    }

    @Override
    public boolean isSameConnection(IConnection connection) {
        return fromPos.isSameConnection(connection) || connection.equals(this);
    }

    @Override
    public SimpleConnection getThisConnection() {
        return getFromPos();
    }

    @Override
    public void updateTubeSegments(Level level) {
        tubeSegments = tubeSegments == 1 ? 2 : 1;
        BlockState state = level.getBlockState(fromPos.pos());
        level.updateNeighborsAt(fromPos.pos(), state.getBlock());
        level.sendBlockUpdated(fromPos.pos(), state, state, 3);
    }

    @Override
    public String toString() {
        return "BezierConnection{" +
                "fromPos=" + fromPos +
                ", toPos=" + toPos +
                ", isValid=" + valid +
                '}';
    }
}