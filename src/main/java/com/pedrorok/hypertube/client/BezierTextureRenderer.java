package com.pedrorok.hypertube.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.pedrorok.hypertube.HypertubeMod;
import com.pedrorok.hypertube.blocks.IBezierProvider;
import com.pedrorok.hypertube.blocks.blockentities.HypertubeBlockEntity;
import com.pedrorok.hypertube.managers.placement.BezierConnection;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.List;

/**
 * @author Rok, Pedro Lucas nmm. Created on 22/05/2025
 * @project Create Hypertube
 */
@OnlyIn(Dist.CLIENT)
public class BezierTextureRenderer<T extends IBezierProvider> implements BlockEntityRenderer<HypertubeBlockEntity> {

    private static final float TUBE_RADIUS = 0.7F;
    private static final float INNER_TUBE_RADIUS = 0.62F;
    private static final int SEGMENTS_AROUND = 4;

    private static final float TILING_UNIT = 1f;

    private final ResourceLocation textureLocation;

    public BezierTextureRenderer(BlockEntityRendererProvider.Context context) {
        this.textureLocation = ResourceLocation.fromNamespaceAndPath(HypertubeMod.MOD_ID, "textures/block/entity_tube_base.png");
    }

    @Override
    public void render(HypertubeBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource,
                       int packedLight, int packedOverlay) {
        BezierConnection connection = blockEntity.getBezierConnection();

        if (connection == null || !connection.getValidation().valid()) {
            return;
        }

        List<Vec3> bezierPoints = connection.getBezierPoints();
        if (bezierPoints.size() < 2) {
            return;
        }

        poseStack.pushPose();
        Vec3 blockPos = Vec3.atLowerCornerOf(blockEntity.getBlockPos());
        poseStack.translate(-blockPos.x, -blockPos.y, -blockPos.z);

        Matrix4f pose = poseStack.last().pose();
        Level level = blockEntity.getLevel();

        VertexConsumer builderExterior = bufferSource.getBuffer(RenderType.entityTranslucentCull(textureLocation));
        renderTubeSegments(bezierPoints, builderExterior, pose, level, packedLight, packedOverlay, false);

        VertexConsumer builderInterior = bufferSource.getBuffer(RenderType.entityTranslucent(textureLocation));
        renderTubeSegments(bezierPoints, builderInterior, pose, level, packedLight, packedOverlay, true);

        poseStack.popPose();
    }

    private void renderTubeSegments(List<Vec3> points, VertexConsumer builder, Matrix4f pose, Level level, int packedLight, int packedOverlay, boolean isInterior) {
        float currentDistance = 0;
        float radius = isInterior ? INNER_TUBE_RADIUS : TUBE_RADIUS;

        for (int i = 0; i < points.size() - 1; i++) {
            Vec3 current = points.get(i);
            Vec3 next = points.get(i + 1);

            Vec3 direction = next.subtract(current);
            float segmentLength = (float) direction.length();

            if (segmentLength < 0.001f) continue;

            Vec3 dirNormalized = direction.normalize();

            Vector3f dirVector = new Vector3f((float) dirNormalized.x, (float) dirNormalized.y, (float) dirNormalized.z);
            Vector3f perpA = findPerpendicularVector(dirVector);
            Vector3f perpB = new Vector3f();
            perpA.cross(dirVector, perpB);
            perpB.normalize();

            float uStart = currentDistance / TILING_UNIT;
            float uEnd = (currentDistance + segmentLength) / TILING_UNIT;

            boolean zFightFix = false;
            for (int j = 0; j < SEGMENTS_AROUND; j++) {
                float angle1 = (float) (j * 2 * Math.PI / SEGMENTS_AROUND) + (float) (Math.PI / 4);
                float angle2 = (float) ((j + 1) * 2 * Math.PI / SEGMENTS_AROUND) + (float) (Math.PI / 4);

                Vector3f offsetStart1 = getOffset(perpA, perpB, angle1, radius);
                Vector3f offsetStart2 = getOffset(perpA, perpB, angle2, radius);

                float v1 = j / (float) SEGMENTS_AROUND;
                float v2 = (j + 1) / (float) SEGMENTS_AROUND;

                if (!isInterior) {
                    addVertex(builder, pose, current, offsetStart1, uStart, v1, packedLight, packedOverlay, false, zFightFix);
                    addVertex(builder, pose, next, offsetStart1, uEnd, v1, packedLight, packedOverlay, false, zFightFix);
                    addVertex(builder, pose, next, offsetStart2, uEnd, v2, packedLight, packedOverlay, false, zFightFix);
                    addVertex(builder, pose, current, offsetStart2, uStart, v2, packedLight, packedOverlay, false, zFightFix);
                }
                addVertex(builder, pose, current, offsetStart2, uStart, v2, packedLight, packedOverlay, true, zFightFix);
                addVertex(builder, pose, next, offsetStart2, uEnd, v2, packedLight, packedOverlay, true, zFightFix);
                addVertex(builder, pose, next, offsetStart1, uEnd, v1, packedLight, packedOverlay, true, zFightFix);
                addVertex(builder, pose, current, offsetStart1, uStart, v1, packedLight, packedOverlay, true, zFightFix);
                zFightFix = !zFightFix;
            }

            currentDistance += segmentLength;
        }
    }

    private void addVertex(VertexConsumer builder, Matrix4f pose,
                           Vec3 pos, Vector3f offset, float u, float v, int light, int overlay, boolean invertLight, boolean zFightFix) {
        float x = (float) pos.x + offset.x;
        float y = (float) pos.y + offset.y;
        float z = (float) pos.z + offset.z;

        float radius = invertLight ? INNER_TUBE_RADIUS : TUBE_RADIUS;

        float normalMultiplier = invertLight ? -0.8f : 0.8f;

        float nx = (offset.x / radius) * normalMultiplier;
        float ny = (offset.y / radius) * normalMultiplier;
        float nz = (offset.z / radius) * normalMultiplier;

        if (zFightFix) {
            pose.translate(0, 0.00001f, 0);
        }
        builder.addVertex(pose, x, y, z)
                .setColor(255, 255, 255, 255)
                .setUv(u, v)
                .setOverlay(overlay)
                .setUv2(light & 0xFFFF, light >> 16)
                .setNormal(nx, ny, nz);
    }

    private Vector3f findPerpendicularVector(Vector3f vec) {
        Vector3f perpendicular;

        if (Math.abs(vec.x) < Math.abs(vec.y) && Math.abs(vec.x) < Math.abs(vec.z)) {
            perpendicular = new Vector3f(1, 0, 0);
        } else if (Math.abs(vec.y) < Math.abs(vec.z)) {
            perpendicular = new Vector3f(0, 1, 0);
        } else {
            perpendicular = new Vector3f(0, 0, 1);
        }

        Vector3f result = new Vector3f();
        vec.cross(perpendicular, result);
        return result.normalize();
    }

    private Vector3f getOffset(Vector3f perpA, Vector3f perpB, float angle, float radius) {
        return new Vector3f(
                (Mth.cos(angle) * perpA.x + Mth.sin(angle) * perpB.x) * radius,
                (Mth.cos(angle) * perpA.y + Mth.sin(angle) * perpB.y) * radius,
                (Mth.cos(angle) * perpA.z + Mth.sin(angle) * perpB.z) * radius
        );
    }

    @Override
    public boolean shouldRenderOffScreen(HypertubeBlockEntity blockEntity) {
        return true;
    }

    @Override
    public boolean shouldRender(HypertubeBlockEntity p_173568_, Vec3 p_173569_) {
        return true;
    }

    @Override
    public @NotNull AABB getRenderBoundingBox(HypertubeBlockEntity blockEntity) {
        return AABB.INFINITE;
    }
}