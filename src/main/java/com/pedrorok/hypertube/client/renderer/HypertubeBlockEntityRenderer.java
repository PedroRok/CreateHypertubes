package com.pedrorok.hypertube.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.pedrorok.hypertube.blocks.blockentities.HypertubeBlockEntity;
import com.pedrorok.hypertube.client.BezierTextureRenderer;
import com.pedrorok.hypertube.core.connection.BezierConnection;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

/**
 * @author Rok, Pedro Lucas nmm. Created on 25/06/2025
 * @project Create Hypertube
 */
public class HypertubeBlockEntityRenderer implements BlockEntityRenderer<HypertubeBlockEntity> {

    private final BezierTextureRenderer tubeRenderer = BezierTextureRenderer.get();

    public HypertubeBlockEntityRenderer(BlockEntityRendererProvider.Context context) {

    }

    @Override
    public void render(HypertubeBlockEntity blockEntity, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (blockEntity.getConnectionOne() instanceof BezierConnection bezierConnectionOne) {
            tubeRenderer.renderBezierConnection(blockEntity.getBlockPos(), bezierConnectionOne, poseStack, bufferSource, packedLight, packedOverlay);
        }
        if (blockEntity.getConnectionTwo() instanceof BezierConnection bezierConnectionTwo) {
            tubeRenderer.renderBezierConnection(blockEntity.getBlockPos(), bezierConnectionTwo, poseStack, bufferSource, packedLight, packedOverlay);
        }
    }

    @Override
    public boolean shouldRenderOffScreen(@NotNull HypertubeBlockEntity blockEntity) {
        return true;
    }

    @Override
    public boolean shouldRender(@NotNull HypertubeBlockEntity blockEntity, @NotNull Vec3 pos) {
        return true;
    }
}
