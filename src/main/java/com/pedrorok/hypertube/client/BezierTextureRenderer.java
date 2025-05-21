package com.pedrorok.hypertube.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.pedrorok.hypertube.blocks.IBezierProvider;
import com.pedrorok.hypertube.blocks.blockentities.HypertubeBlockEntity;
import com.pedrorok.hypertube.managers.placement.BezierConnection;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.List;

/**
 * Renderizador otimizado para tubos que seguem curvas de Bezier
 */
@OnlyIn(Dist.CLIENT)
public class BezierTextureRenderer<T extends IBezierProvider> implements BlockEntityRenderer<HypertubeBlockEntity> {

    private static final float TUBE_RADIUS = 0.7F;
    private static final int SEGMENTS_AROUND = 4;

    private static final float TILING_UNIT = 1f;

    private final BlockEntityRendererProvider.Context context;
    private final ResourceLocation textureLocation;

    public BezierTextureRenderer(BlockEntityRendererProvider.Context context, ResourceLocation textureLocation) {
        this.context = context;
        this.textureLocation = textureLocation;
    }

    @Override
    public void render(HypertubeBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource,
                       int packedLight, int packedOverlay) {
        BezierConnection connection = blockEntity.getBezierConnection();

        if (connection == null || !connection.isValid()) {
            return;
        }

        List<Vec3> bezierPoints = connection.getBezierPoints();
        if (bezierPoints.size() < 2) {
            return;
        }

        // Aplicar transformações do mundo
        poseStack.pushPose();
        Vec3 blockPos = Vec3.atLowerCornerOf(blockEntity.getBlockPos());
        poseStack.translate(-blockPos.x, -blockPos.y, -blockPos.z);


        // Cache nas matrices de transformação
        Matrix4f pose = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();

        // Pré-calcular distância total para mapeamento UV
        float totalLength = calculateTotalLength(bezierPoints);

        // Configurar renderizador de vértices
        VertexConsumer builder = bufferSource.getBuffer(RenderType.entityTranslucent(textureLocation));


        // Renderizar cada segmento da curva
        renderTubeSegments(bezierPoints, builder, pose, normal, null, totalLength, packedLight, packedOverlay);

        poseStack.popPose();
    }

    private void renderTubeSegments(List<Vec3> points, VertexConsumer builder, Matrix4f pose, Matrix3f normalMatrix,
                                    TextureAtlasSprite sprite, float totalLength, int packedLight, int packedOverlay) {
        float currentDistance = 0;

        // Para cada segmento entre pontos consecutivos
        for (int i = 0; i < points.size() - 1; i++) {
            Vec3 current = points.get(i);
            Vec3 next = points.get(i + 1);

            // Calcular a direção do segmento
            Vec3 direction = next.subtract(current);
            float segmentLength = (float) direction.length();

            if (segmentLength < 0.001f) continue;

            // Normalizar o vetor direção
            Vec3 dirNormalized = direction.normalize();

            // Calcular os vetores perpendiculares para os vértices do tubo
            Vector3f dirVector = new Vector3f((float)dirNormalized.x, (float)dirNormalized.y, (float)dirNormalized.z);
            Vector3f perpA = findPerpendicularVector(dirVector);
            Vector3f perpB = new Vector3f();
            perpA.cross(dirVector, perpB);
            perpB.normalize();

            // Calcular coordenadas UV para o segmento
            float uStart = currentDistance / TILING_UNIT;
            float uEnd = (currentDistance + segmentLength) / TILING_UNIT;

            // Criar os vértices ao redor do segmento do tubo
            for (int j = 0; j < SEGMENTS_AROUND; j++) {
                float angle1 = (float) (j * 2 * Math.PI / SEGMENTS_AROUND) + (float) (Math.PI / 4);
                float angle2 = (float) ((j + 1) * 2 * Math.PI / SEGMENTS_AROUND) + (float) (Math.PI / 4);

                // Calcular os offsets para os quatro cantos do quad
                Vector3f offsetStart1 = getOffset(perpA, perpB, angle1);
                Vector3f offsetStart2 = getOffset(perpA, perpB, angle2);

                // Calcular coordenadas V baseadas no ângulo ao redor do tubo
                float v1 = j / (float) SEGMENTS_AROUND;
                float v2 = (j + 1) / (float) SEGMENTS_AROUND;

                // Adicionar os vértices do quad
                addVertex(builder, pose, normalMatrix, current, offsetStart1, uStart, v1, packedLight, packedOverlay);
                addVertex(builder, pose, normalMatrix, next, offsetStart1, uEnd, v1, packedLight, packedOverlay);
                addVertex(builder, pose, normalMatrix, next, offsetStart2, uEnd, v2, packedLight, packedOverlay);
                addVertex(builder, pose, normalMatrix, current, offsetStart2, uStart, v2, packedLight, packedOverlay);
            }

            currentDistance += segmentLength;
        }
    }

    /**
     * Adiciona um vértice ao buffer
     */
    private void addVertex(VertexConsumer builder, Matrix4f pose, Matrix3f normalMatrix,
                           Vec3 pos, Vector3f offset, float u, float v, int light, int overlay) {
        float x = (float)pos.x + offset.x;
        float y = (float)pos.y + offset.y;
        float z = (float)pos.z + offset.z;

        // Normal aproximada (direção do offset)
        float nx = offset.x / TUBE_RADIUS;
        float ny = offset.y / TUBE_RADIUS;
        float nz = offset.z / TUBE_RADIUS;

        builder.addVertex(pose, x, y, z)
                .setColor(255, 255, 255, 255)
                .setUv(u, v)
                .setOverlay(overlay)
                .setUv2(light & 0xFFFF, light >> 16)
                .setNormal(nx, ny, nz);
    }

    /**
     * Encontra um vetor perpendicular a um vetor dado
     */
    private Vector3f findPerpendicularVector(Vector3f vec) {
        Vector3f perpendicular;

        // Escolher o menor componente para calcular o vetor perpendicular
        if (Math.abs(vec.x) < Math.abs(vec.y) && Math.abs(vec.x) < Math.abs(vec.z)) {
            perpendicular = new Vector3f(1, 0, 0);
        } else if (Math.abs(vec.y) < Math.abs(vec.z)) {
            perpendicular = new Vector3f(0, 1, 0);
        } else {
            perpendicular = new Vector3f(0, 0, 1);
        }

        // Produto cruzado para obter um vetor perpendicular
        Vector3f result = new Vector3f();
        vec.cross(perpendicular, result);
        return result.normalize();
    }

    /**
     * Calcula o offset para um ponto do tubo com base nos vetores perpendiculares e ângulo
     */
    private Vector3f getOffset(Vector3f perpA, Vector3f perpB, float angle) {
        return new Vector3f(
                (Mth.cos(angle) * perpA.x + Mth.sin(angle) * perpB.x) * TUBE_RADIUS,
                (Mth.cos(angle) * perpA.y + Mth.sin(angle) * perpB.y) * TUBE_RADIUS,
                (Mth.cos(angle) * perpA.z + Mth.sin(angle) * perpB.z) * TUBE_RADIUS
        );
    }

    /**
     * Calcula o comprimento total da curva
     */
    private float calculateTotalLength(List<Vec3> points) {
        float length = 0;
        for (int i = 0; i < points.size() - 1; i++) {
            length += points.get(i).distanceTo(points.get(i + 1));
        }
        return length;
    }

    @Override
    public boolean shouldRenderOffScreen(HypertubeBlockEntity blockEntity) {
        return true; // Renderizar mesmo quando fora da tela para conexões longas
    }

    @Override
    public boolean shouldRender(HypertubeBlockEntity p_173568_, Vec3 p_173569_) {
        return true; // Renderizar mesmo quando fora da tela para conexões longas
    }

    @Override
    public @NotNull AABB getRenderBoundingBox(HypertubeBlockEntity blockEntity) {
        return AABB.INFINITE;
    }
}