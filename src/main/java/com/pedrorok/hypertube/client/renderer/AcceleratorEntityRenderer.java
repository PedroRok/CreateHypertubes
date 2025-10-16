package com.pedrorok.hypertube.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.pedrorok.hypertube.blocks.HyperAcceleratorBlock;
import com.pedrorok.hypertube.blocks.blockentities.HyperAcceleratorBlockEntity;
import com.pedrorok.hypertube.client.BezierTextureRenderer;
import com.pedrorok.hypertube.core.connection.BezierConnection;
import com.pedrorok.hypertube.registry.ModPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
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
 * @author Rok, Pedro Lucas nmm. Created on 02/06/2025
 * @project Create Hypertube
 */
public class AcceleratorEntityRenderer implements BlockEntityRenderer<HyperAcceleratorBlockEntity> {

    private final BezierTextureRenderer tubeRenderer = BezierTextureRenderer.get();

    @Override
    public void render(HyperAcceleratorBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
                              int light, int overlay) {

        BlockState blockState = be.getBlockState();
        if (!(blockState.getBlock() instanceof HyperAcceleratorBlock)) {
            return;
        }

        if (be.getConnectionOne() instanceof BezierConnection bezierConnectionOne) {
            tubeRenderer.renderBezierConnection(be.getBlockPos(), bezierConnectionOne, ms, buffer, light, overlay);
        }
        if (be.getConnectionTwo() instanceof BezierConnection bezierConnectionTwo) {
            tubeRenderer.renderBezierConnection(be.getBlockPos(), bezierConnectionTwo, ms, buffer, light, overlay);
        }
    }


    @Override
    public boolean shouldRenderOffScreen(HyperAcceleratorBlockEntity p_112306_) {
        return true;
    }

    @Override
    public boolean shouldRender(HyperAcceleratorBlockEntity p_173568_, Vec3 p_173569_) {
        return true;
    }

    @Override
    public @NotNull AABB getRenderBoundingBox(@NotNull HyperAcceleratorBlockEntity blockEntity) {
        return AABB.INFINITE;
    }
}