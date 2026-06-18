package com.pedrorok.hypertube.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.pedrorok.hypertube.blocks.HyperAcceleratorBlock;
import com.pedrorok.hypertube.blocks.HyperEntranceBlock;
import com.pedrorok.hypertube.blocks.HyperJunctionBlock;
import com.pedrorok.hypertube.blocks.blockentities.HyperJunctionBlockEntity;
import com.pedrorok.hypertube.client.BezierTextureRenderer;
import com.pedrorok.hypertube.core.connection.BezierConnection;
import com.pedrorok.hypertube.utils.RenderUtils;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

/**
 * @author Rok, Pedro Lucas nmm. Created on 25/06/2025
 * @project Create Hypertube
 */
public class JunctionEntityRenderer implements BlockEntityRenderer<HyperJunctionBlockEntity> {

    private final BezierTextureRenderer tubeRenderer = BezierTextureRenderer.get();

    public JunctionEntityRenderer(BlockEntityRendererProvider.Context context) {

    }

    @Override
    public void render(HyperJunctionBlockEntity blockEntity, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight, int packedOverlay) {


        BlockState blockState = blockEntity.getBlockState();
        if (!(blockState.getBlock() instanceof HyperJunctionBlock)) {
            return;
        }
        Direction facing = blockState.getValue(HyperAcceleratorBlock.FACING);
        blockEntity.getTubeAttachments().forEach((direct, attachment) -> {
            SuperByteBuffer smartTubeModel = CachedBuffers.partial(attachment.getPartialModel(blockState, blockEntity, direct), blockState);

            RenderUtils.rotateToFace(smartTubeModel, facing, direct.getOpposite(), false);
            smartTubeModel.light(packedLight);
            smartTubeModel.renderInto(poseStack, bufferSource.getBuffer(RenderType.translucent()));
        });

        if (blockEntity.getConnectionOne() instanceof BezierConnection bezierConnectionOne) {
            tubeRenderer.renderBezierConnection(blockEntity.getBlockPos(), bezierConnectionOne, poseStack, bufferSource, packedLight, packedOverlay);
        }
        if (blockEntity.getConnectionTwo() instanceof BezierConnection bezierConnectionTwo) {
            tubeRenderer.renderBezierConnection(blockEntity.getBlockPos(), bezierConnectionTwo, poseStack, bufferSource, packedLight, packedOverlay);
        }
        if (blockEntity.getConnectionThree() instanceof BezierConnection bezierConnectionThree) {
            tubeRenderer.renderBezierConnection(blockEntity.getBlockPos(), bezierConnectionThree, poseStack, bufferSource, packedLight, packedOverlay);
        }
    }

    @Override
    public boolean shouldRenderOffScreen(@NotNull HyperJunctionBlockEntity blockEntity) {
        return true;
    }

    @Override
    public boolean shouldRender(@NotNull HyperJunctionBlockEntity blockEntity, @NotNull Vec3 pos) {
        return true;
    }

    @Override
    public @NotNull AABB getRenderBoundingBox(@NotNull HyperJunctionBlockEntity blockEntity) {
        return AABB.INFINITE;
    }
}
