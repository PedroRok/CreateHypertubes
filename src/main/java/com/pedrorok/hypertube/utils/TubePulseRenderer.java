package com.pedrorok.hypertube.utils;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.pedrorok.hypertube.core.connection.BezierConnection;
import com.pedrorok.hypertube.core.connection.SimpleConnection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Rok, Pedro Lucas nmm. 17/06/2026
 * @project Create Hypertube
 */
@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public final class TubePulseRenderer {

    private static final List<TubePulseEffect> ACTIVE_EFFECTS = new CopyOnWriteArrayList<>();

    private static final float RING_RADIUS = 0.72F;
    private static final float RING_THICKNESS = 1 / 4f;
    private static final int RING_SEGMENTS = 4;

    private static final int DEFAULT_RING_COUNT = 3;
    private static final float DEFAULT_RING_SPACING = 1.5f;
    private static final float DEFAULT_SPEED = 0.6f;
    private static final int DEFAULT_COLOR = 0x55FF55;
    private static final int DEFAULT_FADE_OUT_DISTANCE = 2;

    private TubePulseRenderer() {
    }

    public static void start(BlockPos originBlockPos, BezierConnection connection, boolean invertDir) {
        start(originBlockPos, connection, invertDir, DEFAULT_RING_COUNT, DEFAULT_RING_SPACING, DEFAULT_SPEED, DEFAULT_COLOR, DEFAULT_FADE_OUT_DISTANCE);
    }

    public static void start(BlockPos originBlockPos, BezierConnection connection, boolean invertDir,
                             int ringCount, float ringSpacing, float speed, int color, int fadeOutDistance) {
        Level level = Minecraft.getInstance().level;
        if (level == null || connection == null) return;

        List<Vec3> relativePoints = connection.getRelativeBezierPoints(level, originBlockPos);
        if (relativePoints.size() < 2) return;
        if (invertDir) {
            relativePoints = relativePoints.reversed();
        }
        ACTIVE_EFFECTS.add(new TubePulseEffect(originBlockPos, relativePoints, ringCount, ringSpacing, speed, color, fadeOutDistance));
    }

    public static void start(BlockPos originBlockPos, SimpleConnection simpleConnection, boolean invertDir) {
        start(originBlockPos, simpleConnection, invertDir, DEFAULT_RING_COUNT, DEFAULT_RING_SPACING, DEFAULT_SPEED, DEFAULT_COLOR);
    }

    public static void start(BlockPos originBlockPos, SimpleConnection simpleConnection, boolean invertDir,
                             int ringCount, float ringSpacing, float speed, int color) {
        Level level = Minecraft.getInstance().level;
        if (level == null || simpleConnection == null) return;

        BezierConnection entrance = simpleConnection.getThisEntranceConnection(level);
        if (entrance == null) return;

        start(originBlockPos, entrance, invertDir, ringCount, ringSpacing, speed, color, DEFAULT_FADE_OUT_DISTANCE);
    }

    public static void clear() {
        ACTIVE_EFFECTS.clear();
    }

    // Event

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
        if (ACTIVE_EFFECTS.isEmpty()) return;

        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(true);

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        VertexConsumer builder = bufferSource.getBuffer(RenderType.lines());

        Vec3 camPos = event.getCamera().getPosition();

        for (TubePulseEffect effect : ACTIVE_EFFECTS) {
            effect.tick(partialTick);
            if (effect.isFinished()) continue;

            renderEffect(effect, poseStack, builder, camPos);
        }

        ACTIVE_EFFECTS.removeIf(TubePulseEffect::isFinished);
    }

    // Render
    private static void renderEffect(TubePulseEffect effect, PoseStack poseStack, VertexConsumer builder, Vec3 camPos) {
        List<Vec3> points = effect.getRelativePoints();
        BlockPos origin = effect.getOriginBlockPos();

        Vec3 originAbsolute = new Vec3(origin.getX(), origin.getY(), origin.getZ());
        Vec3 renderOrigin = originAbsolute.subtract(camPos);

        poseStack.pushPose();
        poseStack.translate(renderOrigin.x, renderOrigin.y, renderOrigin.z);
        Matrix4f pose = poseStack.last().pose();

        float traveled = effect.getTravelDistance();
        float spacing = effect.getRingSpacing();
        int color = effect.getColor();

        for (int ring = 0; ring < effect.getRingCount(); ring++) {
            float distanceAlongPath = traveled - ring * spacing;
            if (distanceAlongPath < 0) continue;

            RingTransform transform = resolveRingTransform(points, distanceAlongPath);
            if (transform == null) continue;

            drawRing(builder, pose, transform, effect.getColorFromProgress(), effect.getOpacity());
        }

        poseStack.popPose();
    }

    private static RingTransform resolveRingTransform(List<Vec3> points, float distanceAlongPath) {
        float remaining = distanceAlongPath;

        for (int i = 0; i < points.size() - 1; i++) {
            Vec3 current = points.get(i);
            Vec3 next = points.get(i + 1);
            float segmentLength = (float) current.distanceTo(next);
            if (segmentLength < 1e-6f) continue;

            if (remaining <= segmentLength) {
                float t = remaining / segmentLength;
                Vec3 center = current.lerp(next, t);
                Vector3f tangent = new Vector3f(
                        (float) (next.x - current.x),
                        (float) (next.y - current.y),
                        (float) (next.z - current.z)
                ).normalize();

                Vector3f[] perpendiculars = computeStablePerpendiculars(points, i, tangent);
                return new RingTransform(center, perpendiculars[0], perpendiculars[1]);
            }

            remaining -= segmentLength;
        }

        return null;
    }

    private static Vector3f[] computeStablePerpendiculars(List<Vec3> points, int segmentIndex, Vector3f tangent) {
        Vector3f upVector = new Vector3f(0, 1, 0);
        Vector3f perpA = new Vector3f();
        Vector3f perpB = new Vector3f();

        float upAlignment = Math.abs(tangent.dot(upVector));

        if (upAlignment > 0.999f) {
            Vector3f xAxis = new Vector3f(1, 0, 0);
            Vector3f zAxis = new Vector3f(0, 0, 1);
            float xDot = Math.abs(tangent.dot(xAxis));
            float zDot = Math.abs(tangent.dot(zAxis));
            Vector3f chosenAxis = (xDot < zDot) ? xAxis : zAxis;

            tangent.cross(chosenAxis, perpA);
            perpA.normalize();
        } else {
            Vector3f projectedUp = new Vector3f(upVector);
            float dotProduct = tangent.dot(upVector);
            Vector3f tangentComponent = new Vector3f(tangent).mul(dotProduct);
            projectedUp.sub(tangentComponent);
            projectedUp.normalize();
            perpA.set(projectedUp);
        }

        tangent.cross(perpA, perpB);
        perpB.normalize();

        return new Vector3f[]{perpA, perpB};
    }

    private static void drawRing(VertexConsumer builder, Matrix4f pose, RingTransform transform, int color, int opacity) {
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        List<Vector3f> ringPoints = new ArrayList<>(RING_SEGMENTS);
        for (int i = 0; i < RING_SEGMENTS; i++) {
            float angle = (float) (i * 2 * Math.PI / RING_SEGMENTS) + (float) (Math.PI / 4);
            ringPoints.add(offset(transform.perpA(), transform.perpB(), angle));
        }

        for (int i = 0; i < RING_SEGMENTS; i++) {
            Vector3f a = ringPoints.get(i);
            Vector3f b2 = ringPoints.get((i + 1) % RING_SEGMENTS);
            addLine(builder, pose, transform.center(), a, b2, r, g, b, opacity);
        }
    }

    private static Vector3f offset(Vector3f perpA, Vector3f perpB, float angle) {
        float cosAngle = Mth.cos(angle);
        float sinAngle = Mth.sin(angle);
        return new Vector3f(
                (cosAngle * perpA.x + sinAngle * perpB.x) * RING_RADIUS,
                (cosAngle * perpA.y + sinAngle * perpB.y) * RING_RADIUS,
                (cosAngle * perpA.z + sinAngle * perpB.z) * RING_RADIUS
        );
    }

    private static void addLine(VertexConsumer builder, Matrix4f pose, Vec3 center, Vector3f from, Vector3f to, int r, int g, int b, int opacity) {
        float x1 = (float) center.x + from.x;
        float y1 = (float) center.y + from.y;
        float z1 = (float) center.z + from.z;
        float x2 = (float) center.x + to.x;
        float y2 = (float) center.y + to.y;
        float z2 = (float) center.z + to.z;

        Vector3f normal = new Vector3f(to.x - from.x, to.y - from.y, to.z - from.z).normalize();

        builder.addVertex(pose, x1, y1, z1)
                .setColor(r, g, b, opacity)
                .setNormal(normal.x, normal.y, normal.z);
        builder.addVertex(pose, x2, y2, z2)
                .setColor(r, g, b, opacity)
                .setNormal(normal.x, normal.y, normal.z);
    }

    private record RingTransform(Vec3 center, Vector3f perpA, Vector3f perpB) {
    }
}