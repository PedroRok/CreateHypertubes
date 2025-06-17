package com.pedrorok.hypertube.managers.placement;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.pedrorok.hypertube.blocks.HypertubeBlock;
import com.pedrorok.hypertube.blocks.blockentities.HypertubeBlockEntity;
import com.pedrorok.hypertube.items.HypertubeItem;
import com.pedrorok.hypertube.managers.connection.BezierConnection;
import com.pedrorok.hypertube.managers.connection.SimpleConnection;
import com.pedrorok.hypertube.registry.ModBlocks;
import com.pedrorok.hypertube.registry.ModDataComponent;
import com.pedrorok.hypertube.utils.MessageUtils;
import com.pedrorok.hypertube.utils.RayCastUtils;
import com.pedrorok.hypertube.utils.TubeUtils;
import com.simibubi.create.content.trains.track.TrackBlockOutline;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

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

        Item tubeItem = ModBlocks.HYPERTUBE.get().asItem();
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
        boolean hypertubeHitResult = hitState.getBlock() instanceof HypertubeBlock;
        if (hitState.isAir() || hypertubeHitResult) {
            hoveringPos = pos;
        } else {
            pos = pos.relative(bhr.getDirection());
        }

        SimpleConnection connectionFrom = ModDataComponent.decodeSimpleConnection(stack);

        animation.setValue(0.8);
        if (connectionFrom == null) {
            animation.setValue(0);
            return;
        }

        Direction finalDirection = RayCastUtils.getDirectionFromHitResult(player, ModBlocks.HYPERTUBE.get());

        SimpleConnection connectionTo = new SimpleConnection(pos, finalDirection);
        BezierConnection bezierConnection = BezierConnection.of(connectionFrom, connectionTo);

        // Exception & visual
        ResponseDTO response = bezierConnection.getValidation();

        if (response.valid()) {
            response = TubeUtils.checkSurvivalItems(player, (int) bezierConnection.distance(), true);
        }
        if (response.valid()) {
            response = TubeUtils.checkBlockCollision(level, bezierConnection);
        }
        if (response.valid() && hypertubeHitResult) {
            response = TubeUtils.checkClickedHypertube(level, pos, finalDirection.getOpposite());
        }

        animation.setValue(!response.valid() ? 0.2 : 0.8);

        canPlace = response.valid();
        bezierConnection.drawPath(animation, canPlace);

        if (!response.valid()) {
            MessageUtils.sendActionMessage(player, response.getMessageComponent());
            return;
        }

        MessageUtils.sendActionMessage(player, "");
    }

    public static boolean handleHypertubeClicked(HypertubeBlockEntity tubeEntity, Player player, SimpleConnection simpleConnection, BlockPos pos, Direction direction, Level level, ItemStack stack) {

        boolean thisTubeCanConnTo = tubeEntity.getConnectionTo() == null;
        boolean thisTubeCanConnFrom = tubeEntity.getConnectionFrom() == null;
        HypertubeBlockEntity otherBlockEntity = (HypertubeBlockEntity) level.getBlockEntity(simpleConnection.pos());

        if (otherBlockEntity == null) {
            MessageUtils.sendActionMessage(player, Component.translatable("placement.create_hypertube.no_other_tube_found")
                    .withStyle(ChatFormatting.RED));
            return false;
        }

        boolean otherTubeCanConnTo = otherBlockEntity.getConnectionTo() == null;
        boolean otherTubeCanConnFrom = otherBlockEntity.getConnectionFrom() == null;

        boolean usingConnectingTo = thisTubeCanConnFrom && otherTubeCanConnTo;

        if (!usingConnectingTo) {
            if (!thisTubeCanConnTo || !otherTubeCanConnFrom) {
                MessageUtils.sendActionMessage(player, Component.translatable("placement.create_hypertube.cant_conn_tubes")
                        .withStyle(ChatFormatting.RED));
                return false;
            }
        }

        BezierConnection connection = new BezierConnection(
                usingConnectingTo ? simpleConnection : new SimpleConnection(pos, direction),
                usingConnectingTo ? new SimpleConnection(pos, direction.getOpposite()) : new SimpleConnection(simpleConnection.pos(), simpleConnection.direction().getOpposite()));


        ResponseDTO validation = connection.getValidation();
        if (validation.valid()) {
            validation = TubeUtils.checkSurvivalItems(player, (int) connection.distance(), true);
        }
        if (validation.valid()) {
            validation = TubeUtils.checkBlockCollision(level, connection);
        }
        if (validation.valid()) {
            validation = TubeUtils.checkClickedHypertube(level, pos, direction);
        }

        if (!validation.valid()) {
            MessageUtils.sendActionMessage(player, validation.getMessageComponent().withStyle(ChatFormatting.RED), true);
            return false;
        }
        TubeUtils.checkSurvivalItems(player, (int) connection.distance(), false);

        if (level.isClientSide) {
            connection.drawPath(LerpedFloat.linear()
                    .startWithValue(0), true);
        }

        if (usingConnectingTo) {
            tubeEntity.setConnectionFrom(connection.getFromPos(), direction);
            otherBlockEntity.setConnectionTo(connection);
        } else {
            tubeEntity.setConnectionTo(connection);
            otherBlockEntity.setConnectionFrom(connection.getFromPos(), direction);
        }

        MessageUtils.sendActionMessage(player, Component.translatable("placement.create_hypertube.success_conn")
                .withStyle(ChatFormatting.GREEN), true);
        player.playSound(SoundEvents.ITEM_FRAME_ADD_ITEM, 1.0f, 1.0f);


        HypertubeItem.clearConnection(player.getItemInHand(InteractionHand.MAIN_HAND));
        return true;
    }

    // SERVER BLOCK VALIDATION
    public static void tickPlayerServer(@NotNull Player player) {
        if (player.tickCount % 20 != 0) return;
        ItemStack itemInHand = player.getItemInHand(InteractionHand.MAIN_HAND);
        Level level = player.level();
        if (!(itemInHand.getItem() instanceof HypertubeItem)) return;
        if (!itemInHand.hasFoil()) return;
        SimpleConnection connection = ModDataComponent.decodeSimpleConnection(itemInHand);
        if (connection == null) return;
        if (!(level.getBlockEntity(new BlockPos(connection.pos())) instanceof HypertubeBlockEntity)) {
            HypertubeItem.clearConnection(itemInHand);
            MessageUtils.sendActionMessage(player,
                    Component.translatable("placement.create_hypertube.conn_cleared_invalid_block").withStyle(ChatFormatting.RED)
            );
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void drawCustomBlockSelection(PoseStack ms, MultiBufferSource buffer, Vec3 camera) {
        ItemStack mainHandItem = Minecraft.getInstance().player.getMainHandItem();
        HypertubeBlock hypertubeBlock = ModBlocks.HYPERTUBE.getUnchecked();
        if (hypertubeBlock == null) return;
        if (!mainHandItem.is(hypertubeBlock.asItem())) return;
        if (!mainHandItem.hasFoil()) return;
        SimpleConnection connection = ModDataComponent.decodeSimpleConnection(mainHandItem);
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
