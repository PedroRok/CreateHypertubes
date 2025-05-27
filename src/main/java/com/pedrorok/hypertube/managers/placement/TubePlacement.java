package com.pedrorok.hypertube.managers.placement;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.pedrorok.hypertube.blocks.HypertubeBaseBlock;
import com.pedrorok.hypertube.blocks.HypertubeBlock;
import com.pedrorok.hypertube.registry.ModBlocks;
import com.pedrorok.hypertube.registry.ModDataComponent;
import com.pedrorok.hypertube.utils.RayCastUtils;
import com.simibubi.create.content.trains.track.TrackBlockOutline;
import net.createmod.catnip.animation.LerpedFloat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
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
    static boolean canPlace = false;
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

        Item tubeItem = ModBlocks.HYPERTUBE.asItem();
        if (!stack.getItem().equals(tubeItem)) {
            stack = player.getOffhandItem();
            if (!stack.getItem().equals(tubeItem))
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

        SimpleConnection connectionFrom = stack.get(ModDataComponent.TUBE_CONNECTING_FROM);
        animation.setValue(0.8);
        if (connectionFrom == null) {
            animation.setValue(0);
            return;
        }

        Direction finalDirection = RayCastUtils.getDirectionFromHitResult(player, ModBlocks.HYPERTUBE.get());


        SimpleConnection connectionTo = new SimpleConnection(pos, finalDirection);
        BezierConnection bezierConnection = BezierConnection.of(connectionFrom, connectionTo);
        // Exception & visual
        boolean valid = bezierConnection.isValid();
        animation.setValue( !valid ? 0.2 : 0.8);
        canPlace = valid;
        bezierConnection.drawPath(animation);
    }



    @OnlyIn(Dist.CLIENT)
    public static void drawCustomBlockSelection(PoseStack ms, MultiBufferSource buffer, Vec3 camera) {
        ItemStack mainHandItem = Minecraft.getInstance().player.getMainHandItem();
        if (!mainHandItem.is(ModBlocks.HYPERTUBE.asItem())) return;
        if (!mainHandItem.hasFoil()) return;
        SimpleConnection connection = mainHandItem.get(ModDataComponent.TUBE_CONNECTING_FROM);
        if (connection == null) return;

        Minecraft mc = Minecraft.getInstance();
        BlockState blockState = mc.level.getBlockState(connection.pos());
        if (!(blockState.getBlock() instanceof HypertubeBlock)) return;
        HypertubeBlock block = (HypertubeBlock) blockState.getBlock();

        VertexConsumer vb = buffer.getBuffer(RenderType.lines());
        ms.pushPose();
        ms.translate(connection.pos().getX() - camera.x, connection.pos().getY() - camera.y, connection.pos().getZ() - camera.z);
        TrackBlockOutline.renderShape(block.getShape(blockState), ms, vb, canPlace);
        ms.popPose();
    }
}
