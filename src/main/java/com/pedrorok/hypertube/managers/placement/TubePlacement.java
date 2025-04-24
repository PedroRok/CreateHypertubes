package com.pedrorok.hypertube.managers.placement;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.pedrorok.hypertube.blocks.HypertubeBaseBlock;
import com.pedrorok.hypertube.blocks.HypertubeBlock;
import com.pedrorok.hypertube.items.HypertubeItem;
import com.pedrorok.hypertube.registry.ModBlocks;
import com.pedrorok.hypertube.registry.ModDataComponent;
import com.pedrorok.hypertube.registry.ModItems;
import com.simibubi.create.content.trains.track.*;
import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.data.Pair;
import net.createmod.catnip.outliner.Outliner;
import net.createmod.catnip.theme.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * @author Rok, Pedro Lucas nmm. Created on 23/04/2025
 * @project Create Hypertube
 */
public class TubePlacement {

    static BlockPos hoveringPos;
    static LerpedFloat animation = LerpedFloat.linear()
            .startWithValue(0);

    @OnlyIn(Dist.CLIENT)
    public static void clientTick() {
        LocalPlayer player = Minecraft.getInstance().player;
        ItemStack stack = player.getMainHandItem();
        HitResult hitResult = Minecraft.getInstance().hitResult;

        if (hitResult == null)
            return;
        if (hitResult.getType() != HitResult.Type.BLOCK)
            return;

        InteractionHand hand = InteractionHand.MAIN_HAND;
        if (!stack.getItem().equals(ModItems.HYPERTUBE_ITEM.get())) {
            stack = player.getOffhandItem();
            hand = InteractionHand.OFF_HAND;
            if (!stack.getItem().equals(ModItems.HYPERTUBE_ITEM.get()))
                return;
        }

        if (!stack.hasFoil())
            return;

        HypertubeItem blockItem = (HypertubeItem) stack.getItem();
        Level level = player.level();
        BlockHitResult bhr = (BlockHitResult) hitResult;
        BlockPos pos = bhr.getBlockPos();
        BlockState hitState = level.getBlockState(pos);
        if (hitState.isAir() || hitState.getBlock() instanceof HypertubeBaseBlock) {
            hoveringPos = pos;
        } else {
            pos = pos.relative(bhr.getDirection());
        }

        ConnectingFrom connectingFrom = stack.get(ModDataComponent.TUBE_CONNECTING_FROM);
        animation.setValue(0.8);
        if (connectingFrom == null) {
            animation.setValue(0);
            return;
        }

        line(0, pos.getCenter(), connectingFrom.pos().getCenter());
    }


    @OnlyIn(Dist.CLIENT)
    private static void line(int id, Vec3 v1, Vec3 o1) {
        int color = Color.mixColors(0xEA5C2B, 0x95CD41, animation.getValue());
        Outliner.getInstance().showLine(Pair.of("start", id), v1, o1)
                .lineWidth(1 / 8f)
                .disableLineNormals()
                .colored(color);
    }


    @OnlyIn(Dist.CLIENT)
    public static void drawCustomBlockSelection(PoseStack ms, MultiBufferSource buffer, Vec3 camera) {
        ItemStack mainHandItem = Minecraft.getInstance().player.getMainHandItem();
        if (!mainHandItem.is(ModBlocks.HYPERTUBE.get().asItem())) return;
        if (!mainHandItem.hasFoil()) return;
        ConnectingFrom connectingFrom = mainHandItem.get(ModDataComponent.TUBE_CONNECTING_FROM);
        if (connectingFrom == null) return;

        VertexConsumer vb = buffer.getBuffer(RenderType.lines());
        ms.pushPose();
        ms.translate(connectingFrom.pos().getX() - camera.x, connectingFrom.pos().getY() - camera.y, connectingFrom.pos().getZ() - camera.z);
        TrackBlockOutline.renderShape(HypertubeBlock.SHAPE_CORE, ms, vb, false);
        ms.popPose();
    }
}
