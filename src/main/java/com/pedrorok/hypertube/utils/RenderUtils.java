package com.pedrorok.hypertube.utils;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.createmod.catnip.render.SuperByteBuffer;
import net.createmod.ponder.enums.PonderConfig;
import net.createmod.ponder.enums.PonderGuiTextures;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.Direction;
import org.joml.Matrix4f;

/**
 * @author Rok, Pedro Lucas nmm. Created on 19/11/2025
 * @project Create Hypertube
 */
public class RenderUtils {

    public static void rotateAroundCenterVertical(SuperByteBuffer buffer, int degreesRotated) {
        buffer.translate(0.5f, 0.5f, 0.5f);
        buffer.rotateY((float) Math.toRadians(degreesRotated));
        buffer.translate(-0.5f, -0.5f, -0.5f);
    }

    public static void rotateAroundCenterHorizontalZ(SuperByteBuffer buffer, int degreesRotated) {
        buffer.translate(0.5f, 0.5f, 0.5f);
        buffer.rotateZ((float) Math.toRadians(degreesRotated));
        buffer.translate(-0.5f, -0.5f, -0.5f);
    }

    public static void rotateToFace(SuperByteBuffer model, Direction tubeFacing, Direction attachmentDirection, boolean isTubeVertical) {

        switch (attachmentDirection) {
            case NORTH -> rotateAroundCenterVertical(model, -90);
            case EAST -> rotateAroundCenterVertical(model, 180);
            case SOUTH -> rotateAroundCenterVertical(model, 90);
            case WEST -> rotateAroundCenterVertical(model, 0);
            case UP -> rotateAroundCenterHorizontalZ(model, -90);
            case DOWN -> rotateAroundCenterHorizontalZ(model, 90);
        }

        if (!isTubeVertical) {
            if ((attachmentDirection == Direction.UP || attachmentDirection == Direction.DOWN) && tubeFacing != Direction.NORTH && tubeFacing != Direction.SOUTH) return;
            model.rotateXCenteredDegrees(90);
        }
    }

    public static void directionArrow(PoseStack ms, float centerX, float centerY, float alpha, int color, float snappedAngle) {
        //RenderSystem.enableTexture();
        PonderGuiTextures.PLACEMENT_INDICATOR_SHEET.bind();
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);

        ms.pushPose();
        ms.translate(centerX, centerY, 50);
        float scale = PonderConfig.Client().indicatorScale.get()
                .floatValue() * .75f;
        ms.scale(scale, scale, 1);
        ms.scale(12, 12, 1);

        float index = snappedAngle / 22.5f;
        float tex_size = 16f / 256f;

        float tx = 0;
        float ty = index * tex_size;
        float tw = 1f;
        float th = tex_size;

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        Matrix4f mat = ms.last().pose();
        buffer.vertex(mat, -1, -1, 0).uv(tx, ty).color(r, g, b, alpha).endVertex();
        buffer.vertex(mat, -1, 1, 0).uv(tx, ty + th).color(r, g, b, alpha).endVertex();
        buffer.vertex(mat, 1, 1, 0).uv(tx + tw, ty + th).color(1f, g, b, alpha).endVertex();
        buffer.vertex(mat, 1, -1, 0).uv(tx + tw, ty).color(r, g, b, alpha).endVertex();

        BufferUploader.drawWithShader(buffer.end());

        RenderSystem.disableBlend();
        ms.popPose();
    }
}