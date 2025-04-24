package com.pedrorok.hypertube.managers.placement;

import lombok.AllArgsConstructor;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Rok, Pedro Lucas nmm. Created on 24/04/2025
 * @project Create Hypertube
 */
@Setter
public class BezierConnection {

    public static final float MAX_DISTANCE = 20.0f;

    private final ConnectingFrom from;
    private @Nullable BlockPos toPos;
    private int detailLevel;

    private List<Vec3> bezierPoints;

    public BezierConnection(ConnectingFrom from, @Nullable BlockPos toPos) {
        this(from, toPos, (int) Math.max(3, from.pos().getCenter().distanceTo(toPos.getCenter())));
    }

    public BezierConnection(ConnectingFrom from, @Nullable BlockPos toPos, int detailLevel) {
        this.from = from;
        this.toPos = toPos;
        this.detailLevel = detailLevel;
    }

    public List<Vec3> getBezierPoints(Direction finalDirection) {
        if (bezierPoints != null) return bezierPoints;
        if (toPos == null) return List.of();
        Vec3 fromPos = from.pos().getCenter();
        Vec3 toPos = new Vec3(this.toPos.getX() + 0.5, this.toPos.getY() + 0.5, this.toPos.getZ() + 0.5);
        bezierPoints = calculateBezierCurve(fromPos, from.direction(), toPos, detailLevel, finalDirection);
        return bezierPoints;
    }

    private List<Vec3> calculateBezierCurve(Vec3 from, Direction direction, Vec3 to, int detailLevel, @Nullable Direction finalDirection) {
        double distance = from.distanceTo(to);

        Vec3 controlPoint1 = createFirstControlPoint(from, direction, distance);
        Vec3 controlPoint2 = createSecondControlPoint(to, direction, distance, Vec3.atLowerCornerOf(finalDirection.getNormal()));

        List<Vec3> curvePoints = new ArrayList<>();

        for (int i = 0; i <= detailLevel; i++) {
            double t = (double) i / detailLevel;
            Vec3 point = cubicBezier(from, controlPoint1, controlPoint2, to, t);
            curvePoints.add(point);
        }

        return curvePoints;
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

    public float getMaxAngleBezierAngle(Direction finalDirection) {
        if (bezierPoints == null) {
            bezierPoints = getBezierPoints(finalDirection);
        }
        float maxAngle = 0;
        Vec3 lastPoint = bezierPoints.get(0);
        for (int i = 1; i < bezierPoints.size() - 1; i++) {
            Vec3 currentPoint = bezierPoints.get(i);
            Vec3 nextPoint = bezierPoints.get(i + 1);

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
        return (float) from.pos().getCenter().distanceTo(toPos.getCenter());
    }

    public boolean isValid() {
        return from != null && toPos != null;
    }


    public static BezierConnection of(ConnectingFrom from, @Nullable BlockPos toPos) {
        return new BezierConnection(from, toPos);
    }
}
