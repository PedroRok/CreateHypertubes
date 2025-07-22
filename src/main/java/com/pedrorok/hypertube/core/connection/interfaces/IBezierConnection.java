package com.pedrorok.hypertube.core.connection.interfaces;

import com.pedrorok.hypertube.core.connection.SimpleConnection;
import com.pedrorok.hypertube.core.placement.ResponseDTO;
import com.pedrorok.hypertube.utils.TubeUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.createmod.catnip.animation.LerpedFloat;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author Rok, Pedro Lucas nmm. Created on 22/07/2025
 * @project Create Hypertube
 */
@Getter
public abstract class IBezierConnection<T extends ISimpleConnection<?>> {

    public static final float MAX_DISTANCE = 40.0f;
    public static final float MAX_ANGLE = 0.6f;

    @Getter
    private final UUID uuid = UUID.randomUUID();

    private final T fromPos;
    private @Nullable T toPos;
    private final int detailLevel;


    @Setter
    private List<Vec3> bezierPoints;

    private ResponseDTO valid;

    public IBezierConnection(T fromPos, @Nullable T toPos, int detailLevel) {
        this.fromPos = fromPos;
        this.toPos = toPos;
        this.detailLevel = detailLevel;
    }


    public abstract Vec3 getFromPosCenter();

    public abstract Vec3 getToPosCenter();


    public List<Vec3> getBezierPoints(boolean forced) {
        if (this.bezierPoints != null && !forced) return bezierPoints;
        if (this.toPos == null) return List.of();
        Vec3 fromPos = getFromPosCenter();
        Vec3 toPos = getToPosCenter();
        bezierPoints = calculateBezierCurve(fromPos, this.fromPos.direction(), toPos, detailLevel, this.toPos.direction());
        return bezierPoints;
    }


    private List<Vec3> calculateBezierCurve(Vec3 from, Direction direction, Vec3 toVec, int detailLevel, @Nullable Direction finalDirection) {
        valid = null;
        double distance = from.distanceTo(toVec);

        Vec3 controlPoint1 = createFirstControlPoint(from, direction, distance);
        Vec3 controlPoint2 = createSecondControlPoint(toVec, direction, distance, Vec3.atLowerCornerOf(finalDirection.getNormal()));

        List<Vec3> curvePoints = new ArrayList<>();

        for (int i = 0; i <= detailLevel; i++) {
            double t = (double) i / detailLevel;
            Vec3 point = cubicBezier(from, controlPoint1, controlPoint2, toVec, t);
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

    public float getMaxAngleBezierAngle() {
        if (bezierPoints == null) {
            bezierPoints = getBezierPoints(true);
        }
        // THIS IS TO PREVENT FROM PLACING BACK
        Vec3 first = getBezierPoints(false).getFirst();
        Vec3 second = getBezierPoints(false).get(1);
        Direction direction = fromPos.direction();
        Vec3 firstDirection = new Vec3(direction.getStepX(), direction.getStepY(), direction.getStepZ());
        Vec3 secondDirection = second.subtract(first).normalize();
        float initialAngle = (float) Math.acos(firstDirection.dot(secondDirection) / (firstDirection.length() * secondDirection.length()));
        if (initialAngle >= 2.) {
            return initialAngle;
        }
        // END OF PREVENTION

        return getMaxAngle();
    }

    private float getMaxAngle() {
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
        return (float) getFromPosCenter().distanceTo(getToPosCenter());
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

        /*public BezierConnection invert() {
        List<Vec3> newBezier = new ArrayList<>(bezierPoints);
        Collections.reverse(newBezier);
        return new BezierConnection(new SimpleConnection(toPos.pos(), toPos.direction().getOpposite()), fromPos, tubeSegments, newBezier);
    }*/

    @OnlyIn(Dist.CLIENT)
    public void drawPath(LerpedFloat animation, boolean isValid) {
        Vec3 pos1 = getFromPosCenter();
        int id = 0;
        for (Vec3 bezierPoint : getBezierPoints(false)) {
            TubeUtils.line(uuid, id, pos1, bezierPoint, animation, !isValid);
            pos1 = bezierPoint;
            id++;
        }
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
