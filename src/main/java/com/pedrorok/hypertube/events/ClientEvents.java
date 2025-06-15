package com.pedrorok.hypertube.events;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.pedrorok.hypertube.managers.camera.DetachedCameraController;
import com.pedrorok.hypertube.managers.placement.TubePlacement;
import com.pedrorok.hypertube.managers.sound.TubeSoundManager;
import com.pedrorok.hypertube.managers.travel.TravelConstants;
import net.createmod.catnip.render.DefaultSuperRenderTypeBuffer;
import net.createmod.catnip.render.SuperRenderTypeBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;

/**
 * @author Rok, Pedro Lucas nmm. Created on 23/04/2025
 * @project Create Hypertube
 */
@EventBusSubscriber(Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void onTickPre(ClientTickEvent.Pre event) {
        onTick(true);
    }

    @SubscribeEvent
    public static void onTickPost(ClientTickEvent.Post event) {
        onTick(false);
    }

    private static void onTick(boolean isPreEvent) {
        if (!isGameActive()) return;

        if (isPreEvent) {
            TubeSoundManager.tickClientPlayerSounds();
            return;
        }
        TubePlacement.clientTick();
        DetachedCameraController.tickCamera();
    }


    @SubscribeEvent
    public static void onRenderWorld(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;

        PoseStack ms = event.getPoseStack();
        ms.pushPose();
        SuperRenderTypeBuffer buffer = DefaultSuperRenderTypeBuffer.getInstance();
        Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();

        TubePlacement.drawCustomBlockSelection(ms, buffer, camera);

        buffer.draw();
        RenderSystem.enableCull();
        ms.popPose();
    }

    protected static boolean isGameActive() {
        return !(Minecraft.getInstance().level == null || Minecraft.getInstance().player == null);
    }

    @SubscribeEvent
    public static void onRenderPlayer(RenderPlayerEvent.Pre event) {
        Player player = event.getEntity();
        if (!player.getPersistentData().getBoolean(TravelConstants.TRAVEL_TAG)) return;

        PoseStack poseStack = event.getPoseStack();

        poseStack.pushPose();
        poseStack.translate(0, 0.2, 0);
        poseStack.mulPose(Axis.YP.rotationDegrees(-player.getYRot()));
        poseStack.mulPose(Axis.XP.rotationDegrees(player.getXRot() + 90));
        poseStack.mulPose(Axis.YP.rotationDegrees(player.getYRot()));
        poseStack.translate(0, -0.5, 0);
        poseStack.scale(0.8f, 0.8f, 0.8f);
    }

    @SubscribeEvent
    public static void onRenderPlayerPost(RenderPlayerEvent.Post event) {
        Player player = event.getEntity();
        if (!player.getPersistentData().getBoolean(TravelConstants.TRAVEL_TAG)) return;

        event.getPoseStack().popPose();

    }
}
