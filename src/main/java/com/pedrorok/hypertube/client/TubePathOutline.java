package com.pedrorok.hypertube.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.pedrorok.hypertube.blocks.blockentities.TubePathBlockEntity;
import com.pedrorok.hypertube.core.collision.TubeCollision;
import com.pedrorok.hypertube.core.collision.TubeCollision.Polyline;
import com.pedrorok.hypertube.core.collision.TubeFiller;
import com.pedrorok.hypertube.core.connection.BezierConnection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderHighlightEvent;

import java.util.List;

/**
 * @author Rok, Pedro Lucas nmm.
 * @project Create Hypertube
 */
public final class TubePathOutline {

    private static final float STEP = 0.5f;

    private static final float R = 0f;
    private static final float G = 0f;
    private static final float B = 0f;
    private static final float A = 0.4f;

    private TubePathOutline() {
    }

    public static void renderTubeOutline(RenderHighlightEvent.Block event) {
        Level level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }

        BlockPos hit = event.getTarget().getBlockPos();
        if (!(level.getBlockEntity(hit) instanceof TubePathBlockEntity)) {
            return;
        }
        BezierConnection bezier = TubeFiller.ownerBezier(level, hit);
        if (bezier == null) {
            return;
        }

        event.setCanceled(true);
        draw(event, bezier, bezier.getFromPos().pos(), level);
    }

    private static void draw(RenderHighlightEvent.Block event, BezierConnection bezier, BlockPos origin, Level level) {
        List<Vec3> points = bezier.getBezierPoints(level, origin);
        if (points.size() < 2) {
            return;
        }

        Vec3 camera = event.getCamera().getPosition();
        PoseStack.Pose pose = event.getPoseStack().last();
        VertexConsumer consumer = event.getMultiBufferSource().getBuffer(RenderType.lines());

        Polyline path = Polyline.of(points);
        float length = (float) path.length();
        int steps = Math.max(2, Math.round(length / STEP));

        Vec3[] previous = null;
        for (int i = 0; i <= steps; i++) {
            Vec3[] corners = TubeCollision.crossSectionCorners(path, (double) length * i / steps);
            for (int c = 0; c < 4; c++) {
                corners[c] = corners[c].subtract(camera);
            }
            if (i == 0 || i == steps) {
                loop(consumer, pose, corners);
            }
            if (previous != null) {
                for (int c = 0; c < 4; c++) {
                    line(consumer, pose, previous[c], corners[c]);
                }
            }
            previous = corners;
        }
    }

    private static void loop(VertexConsumer consumer, PoseStack.Pose pose, Vec3[] corners) {
        for (int c = 0; c < 4; c++) {
            line(consumer, pose, corners[c], corners[(c + 1) % 4]);
        }
    }

    private static void line(VertexConsumer consumer, PoseStack.Pose pose, Vec3 a, Vec3 b) {
        Vec3 normal = b.subtract(a);
        if (normal.lengthSqr() < 1.0e-9) {
            return;
        }
        normal = normal.normalize();
        consumer.vertex(pose.pose(), (float) a.x, (float) a.y, (float) a.z)
                .color(R, G, B, A)
                .normal(pose.normal(), (float) normal.x, (float) normal.y, (float) normal.z)
                .endVertex();
        consumer.vertex(pose.pose(), (float) b.x, (float) b.y, (float) b.z)
                .color(R, G, B, A)
                .normal(pose.normal(), (float) normal.x, (float) normal.y, (float) normal.z)
                .endVertex();
    }
}
