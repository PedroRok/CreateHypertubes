package com.pedrorok.hypertube.managers.placement;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.pedrorok.hypertube.blocks.HypertubeBaseBlock;
import com.pedrorok.hypertube.blocks.HypertubeBlock;
import com.pedrorok.hypertube.registry.ModBlocks;
import com.pedrorok.hypertube.registry.ModDataComponent;
import com.pedrorok.hypertube.registry.ModItems;
import com.pedrorok.hypertube.utils.RayCastUtils;
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
import net.minecraft.core.Direction;
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

    /*public static BezierConnection manageBezierConnection(Connecting connectingFrom, Connecting connectingTo) {
        BezierConnection bezierConnection = BezierConnection.of(connectingFrom, connectingTo);
        if (bezierConnection.isValid()) {
            return bezierConnection;
        }
        return null;
    }*/

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

        Level level = player.level();
        BlockHitResult bhr = (BlockHitResult) hitResult;
        BlockPos pos = bhr.getBlockPos();
        BlockState hitState = level.getBlockState(pos);
        boolean hypertubeHitResult = hitState.getBlock() instanceof HypertubeBaseBlock;
        if (hitState.isAir() || hypertubeHitResult) {
            hoveringPos = pos;
        } else {
            pos = pos.relative(bhr.getDirection());
        }

        Connecting connectingFrom = stack.get(ModDataComponent.TUBE_CONNECTING_FROM);
        animation.setValue(0.8);
        if (connectingFrom == null) {
            animation.setValue(0);
            return;
        }

        Direction finalDirection = RayCastUtils.getDirectionFromHitResult(player, ModBlocks.HYPERTUBE.get());


        Connecting connectingTo = new Connecting(pos, finalDirection);
        BezierConnection bezierConnection = BezierConnection.of(connectingFrom, connectingTo);

        // Exception & visual
        animation.setValue( !bezierConnection.isValid() ? 0.2 : 0.8);
        bezierConnection.drawPath(animation);
    }



    @OnlyIn(Dist.CLIENT)
    public static void drawCustomBlockSelection(PoseStack ms, MultiBufferSource buffer, Vec3 camera) {
        ItemStack mainHandItem = Minecraft.getInstance().player.getMainHandItem();
        if (!mainHandItem.is(ModBlocks.HYPERTUBE.get().asItem())) return;
        if (!mainHandItem.hasFoil()) return;
        Connecting connecting = mainHandItem.get(ModDataComponent.TUBE_CONNECTING_FROM);
        if (connecting == null) return;

        VertexConsumer vb = buffer.getBuffer(RenderType.lines());
        ms.pushPose();
        ms.translate(connecting.pos().getX() - camera.x, connecting.pos().getY() - camera.y, connecting.pos().getZ() - camera.z);
        TrackBlockOutline.renderShape(HypertubeBlock.SHAPE_CORE, ms, vb, false);
        ms.popPose();
    }
}
