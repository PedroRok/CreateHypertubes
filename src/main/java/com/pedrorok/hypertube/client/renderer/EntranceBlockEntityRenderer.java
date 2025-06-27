package com.pedrorok.hypertube.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.pedrorok.hypertube.blocks.HyperEntranceBlock;
import com.pedrorok.hypertube.blocks.blockentities.HyperEntranceBlockEntity;
import com.pedrorok.hypertube.blocks.blockentities.HypertubeBlockEntity;
import com.pedrorok.hypertube.client.BezierTextureRenderer;
import com.pedrorok.hypertube.core.connection.BezierConnection;
import com.pedrorok.hypertube.registry.ModPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
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
public class EntranceBlockEntityRenderer extends KineticBlockEntityRenderer<HyperEntranceBlockEntity> {

    private final BezierTextureRenderer tubeRenderer = BezierTextureRenderer.get();

    public EntranceBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(HyperEntranceBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
                              int light, int overlay) {

        BlockState blockState = be.getBlockState();
        if (!(blockState.getBlock() instanceof HyperEntranceBlock)) {
            return;
        }

        Direction facing = blockState.getValue(HyperEntranceBlock.FACING);
        SuperByteBuffer cogwheelModel = CachedBuffers.partialFacingVertical(ModPartialModels.COGWHEEL_HOLE, blockState, facing);

        float angle = getAngleForBe(be, be.getBlockPos(), facing.getAxis());
        Direction.Axis rotationAxisOf = getRotationAxisOf(be);


        kineticRotationTransform(cogwheelModel, be, rotationAxisOf, angle, light);
        cogwheelModel.renderInto(ms, buffer.getBuffer(RenderType.solid()));

        if (be.getConnection() instanceof BezierConnection bezierConnection) {
            tubeRenderer.renderBezierConnection(be.getBlockPos(), bezierConnection, ms, buffer, light, overlay);
        }
    }

    @Override
    public boolean shouldRenderOffScreen(HyperEntranceBlockEntity p_112306_) {
        return true;
    }

    @Override
    public boolean shouldRender(HyperEntranceBlockEntity p_173568_, Vec3 p_173569_) {
        return true;
    }

    @Override
    public @NotNull AABB getRenderBoundingBox(@NotNull HyperEntranceBlockEntity blockEntity) {
        return AABB.INFINITE;
    }
}