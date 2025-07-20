package com.pedrorok.hypertube.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.pedrorok.hypertube.HypertubeMod;
import com.pedrorok.hypertube.core.connection.BezierConnection;
import com.pedrorok.hypertube.core.connection.interfaces.ITubeConnectionEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Rok, Pedro Lucas nmm. Created on 22/05/2025
 * @project Create Hypertube
 */
@OnlyIn(Dist.CLIENT)
public class BezierTextureRenderer {

    private static BezierTextureRenderer INSTANCE;

    private static final float TUBE_RADIUS = 0.7F;
    private static final float INNER_TUBE_RADIUS = 0.62F;
    private static final float LINE_RADIUS = 0.69f;
    private static final int SEGMENTS_AROUND = 4;
    private static final float TILING_UNIT = 1f;

    private static final float UP_ALIGNMENT_THRESHOLD = 0.999f;

    private final ResourceLocation textureTube;
    private final ResourceLocation textureLine;

    public BezierTextureRenderer() {
        this.textureTube = ResourceLocation.fromNamespaceAndPath(HypertubeMod.MOD_ID, "textures/block/tube_base_glass.png");
        this.textureLine = ResourceLocation.fromNamespaceAndPath(HypertubeMod.MOD_ID, "textures/block/tube_base_glass_2.png");
    }

    public void renderBezierConnection(BlockPos blockPosInitial, BezierConnection connection, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (connection == null || !connection.getValidation().valid()) {
            return;
        }
        List<Vec3> bezierPoints = connection.getBezierPoints();
        if (bezierPoints.size() < 2) {
            return;
        }

        Level level = Minecraft.getInstance().level;
        BlockEntity blockEntity = level.getBlockEntity(blockPosInitial);
        if (!(blockEntity instanceof ITubeConnectionEntity)) {
            return;
        }
        int segmentDistance = connection.getTubeSegments();

        poseStack.pushPose();
        Vec3 blockPos = Vec3.atLowerCornerOf(blockPosInitial);
        poseStack.translate(-blockPos.x, -blockPos.y, -blockPos.z);
        Matrix4f pose = poseStack.last().pose();

        List<TubeRing> tubeGeometry = calculateAndCacheGeometry(bezierPoints);

        VertexConsumer builderExterior = bufferSource.getBuffer(RenderType.entityTranslucentCull(textureTube));
        renderComponent(builderExterior, pose, packedLight, packedOverlay, tubeGeometry, SectionType.EXTERIOR, segmentDistance);

        VertexConsumer builderInterior = bufferSource.getBuffer(RenderType.entityTranslucent(textureTube));
        renderComponent(builderInterior, pose, packedLight, packedOverlay, tubeGeometry, SectionType.INTERIOR, segmentDistance);

        VertexConsumer builderLine = bufferSource.getBuffer(RenderType.entityTranslucentCull(textureLine));
        renderComponent(builderLine, pose, packedLight, packedOverlay, tubeGeometry, SectionType.LINE, segmentDistance);

        poseStack.popPose();
    }

    private void renderComponent(VertexConsumer builder, Matrix4f pose, int packedLight, int packedOverlay,
                                 List<TubeRing> tubeGeometry, SectionType sectionType, int segmentDistance) {

        if (tubeGeometry.size() < 2) return;

        boolean interiorTube = sectionType.equals(SectionType.INTERIOR);
        boolean exteriorTube = sectionType.equals(SectionType.EXTERIOR);
        boolean skipLine = sectionType.equals(SectionType.EXTERIOR) || interiorTube;
        boolean doubleSided = sectionType.equals(SectionType.LINE) || exteriorTube;

        for (int i = 0; i < tubeGeometry.size() - 1; i++) {

            if (skipLine && (i % segmentDistance != 0 || (segmentDistance > 1 && (i > tubeGeometry.size() - 3 || i == 0))))
                continue;

            TubeRing current = tubeGeometry.get(i);
            TubeRing next = tubeGeometry.get(i + 1);

            Vec3 dirVec = next.center().subtract(current.center());
            float segmentLength = (float) dirVec.length();
            if (segmentLength < 1e-6) continue;
            Vector3f tangent = new Vector3f((float) dirVec.x, (float) dirVec.y, (float) dirVec.z).normalize();

            List<Vector3f> currentOffsets = interiorTube ? current.interiorOffsets() : (exteriorTube ? current.exteriorOffsets() : current.lineOffsets());
            List<Vector3f> nextOffsets = interiorTube ? next.interiorOffsets() : (exteriorTube ? next.exteriorOffsets() : next.lineOffsets());

            for (int j = 0; j < SEGMENTS_AROUND; j++) {
                int nextJ = (j + 1) % SEGMENTS_AROUND;

                float uStart = current.uCoordinate();

                Vector3f corner_j_movement = new Vector3f(
                        (float) (next.center().x + nextOffsets.get(j).x) - (float) (current.center().x + currentOffsets.get(j).x),
                        (float) (next.center().y + nextOffsets.get(j).y) - (float) (current.center().y + currentOffsets.get(j).y),
                        (float) (next.center().z + nextOffsets.get(j).z) - (float) (current.center().z + currentOffsets.get(j).z)
                );
                float uEnd_j = uStart + (corner_j_movement.dot(tangent) / TILING_UNIT);
                Vector3f corner_nextJ_movement = new Vector3f(
                        (float) (next.center().x + nextOffsets.get(nextJ).x) - (float) (current.center().x + currentOffsets.get(nextJ).x),
                        (float) (next.center().y + nextOffsets.get(nextJ).y) - (float) (current.center().y + currentOffsets.get(nextJ).y),
                        (float) (next.center().z + nextOffsets.get(nextJ).z) - (float) (current.center().z + currentOffsets.get(nextJ).z)
                );
                float uEnd_nextJ = uStart + (corner_nextJ_movement.dot(tangent) / TILING_UNIT);

                float vStart = 0, vEnd = 1;
                if (doubleSided) {
                    addVertex(builder, pose, current.center(), currentOffsets.get(nextJ), uStart, vEnd, packedLight, packedOverlay, false);
                    addVertex(builder, pose, next.center(), nextOffsets.get(nextJ), uEnd_nextJ, vEnd, packedLight, packedOverlay, false);
                    addVertex(builder, pose, next.center(), nextOffsets.get(j), uEnd_j, vStart, packedLight, packedOverlay, false);
                    addVertex(builder, pose, current.center(), currentOffsets.get(j), uStart, vStart, packedLight, packedOverlay, false);
                }

                if (interiorTube) {
                    addVertex(builder, pose, current.center(), currentOffsets.get(nextJ), uStart, vEnd, packedLight, packedOverlay, true);
                    addVertex(builder, pose, next.center(), nextOffsets.get(nextJ), uEnd_nextJ, vEnd, packedLight, packedOverlay, true);
                    addVertex(builder, pose, next.center(), nextOffsets.get(j), uEnd_j, vStart, packedLight, packedOverlay, true);
                    addVertex(builder, pose, current.center(), currentOffsets.get(j), uStart, vStart, packedLight, packedOverlay, true);
                } else {
                    addVertex(builder, pose, current.center(), currentOffsets.get(j), uStart, vStart, packedLight, packedOverlay, doubleSided);
                    addVertex(builder, pose, next.center(), nextOffsets.get(j), uEnd_j, vStart, packedLight, packedOverlay, doubleSided);
                    addVertex(builder, pose, next.center(), nextOffsets.get(nextJ), uEnd_nextJ, vEnd, packedLight, packedOverlay, doubleSided);
                    addVertex(builder, pose, current.center(), currentOffsets.get(nextJ), uStart, vEnd, packedLight, packedOverlay, doubleSided);
                }
            }
        }
    }

    private List<TubeRing> calculateAndCacheGeometry(List<Vec3> points) {
        List<TubeRing> cachedGeometry = new ArrayList<>();
        Vector3f upVector = new Vector3f(0, 1, 0);

        Vector3f lastPerpA = null;
        Vector3f lastPerpB = null;

        for (int i = 0; i < points.size(); i++) {
            Vec3 currentPoint = points.get(i);

            Vector3f tangent = getTangent(points, i, currentPoint);

            Vector3f[] perpendiculars = computeStablePerpendiculars(tangent, upVector, lastPerpA, lastPerpB);
            Vector3f perpA = perpendiculars[0];
            Vector3f perpB = perpendiculars[1];

            lastPerpA = new Vector3f(perpA);
            lastPerpB = new Vector3f(perpB);

            List<Vector3f> ringExterior = generateRingOffsets(perpA, perpB, TUBE_RADIUS);
            List<Vector3f> ringInterior = generateRingOffsets(perpA, perpB, INNER_TUBE_RADIUS);
            List<Vector3f> ringLine = generateRingOffsets(perpA, perpB, LINE_RADIUS);

            cachedGeometry.add(new TubeRing(currentPoint, ringExterior, ringInterior, ringLine, 0.8f / TILING_UNIT));
        }
        return cachedGeometry;
    }

    private static @NotNull Vector3f getTangent(List<Vec3> points, int i, Vec3 currentPoint) {
        Vector3f tangent;
        if (i == points.size() - 1) {
            tangent = new Vector3f((float) (currentPoint.x - points.get(i - 1).x),
                    (float) (currentPoint.y - points.get(i - 1).y),
                    (float) (currentPoint.z - points.get(i - 1).z));
        } else {
            tangent = new Vector3f((float) (points.get(i + 1).x - currentPoint.x),
                    (float) (points.get(i + 1).y - currentPoint.y),
                    (float) (points.get(i + 1).z - currentPoint.z));
        }
        tangent.normalize();
        return tangent;
    }

    private Vector3f[] computeStablePerpendiculars(Vector3f tangent, Vector3f upVector, Vector3f lastPerpA, Vector3f lastPerpB) {
        Vector3f perpA = new Vector3f();
        Vector3f perpB = new Vector3f();

        float upAlignment = Math.abs(tangent.dot(upVector));

        if (upAlignment > UP_ALIGNMENT_THRESHOLD) {
            if (lastPerpA != null && lastPerpB != null) {
                perpA.set(lastPerpA);
                perpB.set(lastPerpB);

                float dotA = Math.abs(tangent.dot(perpA));
                float dotB = Math.abs(tangent.dot(perpB));

                if (dotA > 0.1f || dotB > 0.1f) {
                    getTanCross(tangent, perpA, perpB);
                }
            } else {
                getTanCross(tangent, perpA, perpB);
            }
        } else {

            Vector3f projectedUp = new Vector3f(upVector);
            float dotProduct = tangent.dot(upVector);
            Vector3f tangentComponent = new Vector3f(tangent).mul(dotProduct);
            projectedUp.sub(tangentComponent);
            projectedUp.normalize();

            Vector3f candidatePerpA = new Vector3f(projectedUp);

            if (lastPerpA != null) {
                float dotWithLast = candidatePerpA.dot(lastPerpA);

                if (dotWithLast < 0) {
                    candidatePerpA.negate();
                }
            }

            perpA.set(candidatePerpA);

            tangent.cross(perpA, perpB);
            perpB.normalize();
        }

        return new Vector3f[]{perpA, perpB};
    }

    private void getTanCross(Vector3f tangent, Vector3f perpA, Vector3f perpB) {
        Vector3f xAxis = new Vector3f(1, 0, 0);
        Vector3f zAxis = new Vector3f(0, 0, 1);

        float xDot = Math.abs(tangent.dot(xAxis));
        float zDot = Math.abs(tangent.dot(zAxis));

        Vector3f chosenAxis = (xDot < zDot) ? xAxis : zAxis;

        tangent.cross(chosenAxis, perpA);
        perpA.normalize();

        tangent.cross(perpA, perpB);
        perpB.normalize();
    }

    private void addVertex(VertexConsumer builder, Matrix4f pose,
                           Vec3 pos, Vector3f offset, float u, float v, int light, int overlay, boolean invertNormal) {
        float x = (float) pos.x + offset.x;
        float y = (float) pos.y + offset.y;
        float z = (float) pos.z + offset.z;

        float normalMultiplier = invertNormal ? -1.0f : 1.0f;
        float nx = offset.x * normalMultiplier;
        float ny = offset.y * normalMultiplier;
        float nz = offset.z * normalMultiplier;

        builder.addVertex(pose, x, y, z)
                .setColor(255, 255, 255, 255)
                .setUv(u, v)
                .setOverlay(overlay)
                .setUv2(light & 0xFFFF, light >> 16)
                .setNormal(nx, ny, nz);
    }

    private List<Vector3f> generateRingOffsets(Vector3f perpA, Vector3f perpB, float radius) {
        List<Vector3f> ring = new ArrayList<>();
        for (int j = 0; j < SEGMENTS_AROUND; j++) {
            float angle = (float) (j * 2 * Math.PI / SEGMENTS_AROUND) + (float) (Math.PI / 4);
            ring.add(getOffset(perpA, perpB, angle, radius));
        }
        return ring;
    }

    private Vector3f getOffset(Vector3f perpA, Vector3f perpB, float angle, float radius) {
        float cosAngle = Mth.cos(angle);
        float sinAngle = Mth.sin(angle);
        return new Vector3f(
                (cosAngle * perpA.x + sinAngle * perpB.x) * radius,
                (cosAngle * perpA.y + sinAngle * perpB.y) * radius,
                (cosAngle * perpA.z + sinAngle * perpB.z) * radius
        );
    }

    private enum SectionType {EXTERIOR, INTERIOR, LINE}

    public static BezierTextureRenderer get() {
        if (INSTANCE == null) {
            INSTANCE = new BezierTextureRenderer();
        }
        return INSTANCE;
    }
}