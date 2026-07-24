package com.pedrorok.hypertube.core.collision;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Rok, Pedro Lucas nmm.
 * @project Create Hypertube
 */
public final class TubeCollision {

    public static final double TUBE_HALF = 0.5;

    private static final float STEP = 0.25f;

    private TubeCollision() {
    }

    public static List<BlockPos> occupied(List<Vec3> points) {
        if (points.size() < 2) return List.of();
        Polyline path = Polyline.of(points);
        Set<BlockPos> crossed = new LinkedHashSet<>();

        int steps = steps(path);
        for (int i = 0; i <= steps; i++) {
            crossed.add(BlockPos.containing(path.positionAt(distance(path, i, steps))));
        }

        crossed.remove(BlockPos.containing(points.getFirst()));
        crossed.remove(BlockPos.containing(points.getLast()));
        return List.copyOf(crossed);
    }

    public static List<BlockPos> occupiedVolume(List<Vec3> points) {
        if (points.size() < 2) return List.of();
        Polyline path = Polyline.of(points);
        Set<BlockPos> crossed = new LinkedHashSet<>();

        int steps = steps(path);
        for (int i = 0; i < steps; i++) {
            AABB box = segmentBox(path, distance(path, i, steps), distance(path, i + 1, steps));
            addBlocksOverlapping(crossed, box);
        }

        crossed.remove(BlockPos.containing(points.getFirst()));
        crossed.remove(BlockPos.containing(points.getLast()));
        return List.copyOf(crossed);
    }

    private static void addBlocksOverlapping(Set<BlockPos> out, AABB box) {
        int x0 = Mth.floor(box.minX), x1 = Mth.floor(box.maxX - 1.0e-6);
        int y0 = Mth.floor(box.minY), y1 = Mth.floor(box.maxY - 1.0e-6);
        int z0 = Mth.floor(box.minZ), z1 = Mth.floor(box.maxZ - 1.0e-6);
        for (int x = x0; x <= x1; x++) {
            for (int y = y0; y <= y1; y++) {
                for (int z = z0; z <= z1; z++) {
                    out.add(new BlockPos(x, y, z));
                }
            }
        }
    }

    public static Vec3[] crossSectionCorners(Polyline path, double distance) {
        Vec3 centre = path.positionAt(distance);
        Vec3 tangent = path.tangentAt(distance);
        Vec3 up = upOrthogonalTo(tangent);
        Vec3 right = up.cross(tangent);

        Vec3 side = right.scale(TUBE_HALF);
        Vec3 vert = up.scale(TUBE_HALF);
        return new Vec3[]{
                centre.add(side).add(vert),
                centre.subtract(side).add(vert),
                centre.subtract(side).subtract(vert),
                centre.add(side).subtract(vert)};
    }

    public static List<AABB> boxes(List<Vec3> points, BlockPos pos) {
        if (points.size() < 2) return List.of();
        Polyline path = Polyline.of(points);
        int steps = steps(path);
        List<AABB> boxes = new ArrayList<>();

        for (int i = 0; i < steps; i++) {
            float from = distance(path, i, steps);
            float to = distance(path, i + 1, steps);
            AABB local = clipToBlock(segmentBox(path, from, to), pos);
            if (local != null) {
                boxes.add(local);
            }
        }
        return boxes;
    }

    private static AABB segmentBox(Polyline path, float from, float to) {
        double[] extent = {
                Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY,
                Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY};
        expandCrossSection(extent, path, from);
        expandCrossSection(extent, path, to);
        return new AABB(extent[0], extent[1], extent[2], extent[3], extent[4], extent[5]);
    }

    private static void expandCrossSection(double[] extent, Polyline path, float distance) {
        Vec3 centre = path.positionAt(distance);
        Vec3 tangent = path.tangentAt(distance);
        Vec3 up = upOrthogonalTo(tangent);
        Vec3 right = up.cross(tangent);

        for (int side = -1; side <= 1; side += 2) {
            Vec3 across = centre.add(right.scale(side * TUBE_HALF));
            expandPoint(extent, across.add(up.scale(TUBE_HALF)));
            expandPoint(extent, across.subtract(up.scale(TUBE_HALF)));
        }
    }

    private static void expandPoint(double[] extent, Vec3 point) {
        extent[0] = Math.min(extent[0], point.x);
        extent[1] = Math.min(extent[1], point.y);
        extent[2] = Math.min(extent[2], point.z);
        extent[3] = Math.max(extent[3], point.x);
        extent[4] = Math.max(extent[4], point.y);
        extent[5] = Math.max(extent[5], point.z);
    }

    private static @Nullable AABB clipToBlock(AABB world, BlockPos pos) {
        double minX = Math.max(world.minX, pos.getX());
        double minY = Math.max(world.minY, pos.getY());
        double minZ = Math.max(world.minZ, pos.getZ());
        double maxX = Math.min(world.maxX, pos.getX() + 1.0);
        double maxY = Math.min(world.maxY, pos.getY() + 1.0);
        double maxZ = Math.min(world.maxZ, pos.getZ() + 1.0);
        if (minX >= maxX || minY >= maxY || minZ >= maxZ) {
            return null;
        }
        return new AABB(
                minX - pos.getX(), minY - pos.getY(), minZ - pos.getZ(),
                maxX - pos.getX(), maxY - pos.getY(), maxZ - pos.getZ());
    }

    private static Vec3 upOrthogonalTo(Vec3 tangent) {
        Vec3 up = new Vec3(0, 1, 0).subtract(tangent.scale(tangent.y));
        return up.lengthSqr() < 1.0e-6 ? new Vec3(0, 0, 1) : up.normalize();
    }

    private static int steps(Polyline path) {
        return Math.max(1, (int) Math.round(path.length() / STEP));
    }

    private static float distance(Polyline path, int i, int steps) {
        return (float) (path.length() * i / steps);
    }

    public record Polyline(List<Vec3> points, double[] cumulative, double length) {

        public static Polyline of(List<Vec3> points) {
            double[] cumulative = new double[points.size()];
            double total = 0;
            for (int i = 1; i < points.size(); i++) {
                total += points.get(i).distanceTo(points.get(i - 1));
                cumulative[i] = total;
            }
            return new Polyline(points, cumulative, total);
        }

        public Vec3 positionAt(double d) {
            if (d <= 0) return points.getFirst();
            if (d >= length) return points.getLast();
            int i = segmentIndex(d);
            double segStart = cumulative[i];
            double segLength = cumulative[i + 1] - segStart;
            double t = segLength < 1.0e-9 ? 0 : (d - segStart) / segLength;
            return points.get(i).lerp(points.get(i + 1), t);
        }

        public Vec3 tangentAt(double d) {
            double h = 0.05;
            Vec3 a = positionAt(Math.max(0, d - h));
            Vec3 b = positionAt(Math.min(length, d + h));
            Vec3 tangent = b.subtract(a);
            return tangent.lengthSqr() < 1.0e-9 ? new Vec3(0, 0, 1) : tangent.normalize();
        }

        private int segmentIndex(double d) {
            int lo = 0;
            int hi = points.size() - 1;
            while (lo < hi) {
                int mid = (lo + hi) >>> 1;
                if (cumulative[mid + 1] < d) {
                    lo = mid + 1;
                } else {
                    hi = mid;
                }
            }
            return Math.min(lo, points.size() - 2);
        }
    }
}
