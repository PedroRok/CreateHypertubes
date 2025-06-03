package com.pedrorok.hypertube.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.pedrorok.hypertube.blocks.HyperEntranceBlock;
import com.pedrorok.hypertube.blocks.blockentities.HyperEntranceBlockEntity;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

/**
 * @author Rok, Pedro Lucas nmm. Created on 02/06/2025
 * @project Create Hypertube
 */
public class EntranceBlockEntityRenderer extends KineticBlockEntityRenderer<HyperEntranceBlockEntity> {

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
        SuperByteBuffer cogwheelModel = CachedBuffers.partialFacingVertical(AllPartialModels.SHAFTLESS_COGWHEEL, blockState, facing);

        float angle = getAngleForBe(be, be.getBlockPos(), facing.getAxis());
        Direction.Axis rotationAxisOf = getRotationAxisOf(be);


        kineticRotationTransform(cogwheelModel, be, rotationAxisOf, angle, light);
        cogwheelModel.renderInto(ms, buffer.getBuffer(RenderType.solid()));
    }
}