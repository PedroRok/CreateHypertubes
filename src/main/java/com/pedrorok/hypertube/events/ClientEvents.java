package com.pedrorok.hypertube.events;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.pedrorok.hypertube.managers.camera.DetachedCameraController;
import com.pedrorok.hypertube.managers.TravelManager;
import com.pedrorok.hypertube.managers.placement.TubePlacement;
import com.pedrorok.hypertube.managers.sound.TubeSoundManager;
import com.simibubi.create.foundation.render.SuperRenderTypeBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * @author Rok, Pedro Lucas nmm. Created on 23/04/2025
 * @project Create Hypertube
 */
@Mod.EventBusSubscriber(Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void onTickPre(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        onTick(true);
    }

    @SubscribeEvent
    public static void onTickPost(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        onTick(false);
    }

    private static void onTick(boolean isPreEvent) {
        if (!isGameActive()) return;

        if (isPreEvent) {
            TubeSoundManager.tickClientPlayerSounds();
            return;
        }
        TubePlacement.clientTick();
        DetachedCameraController.cameraTick();
    }


    @SubscribeEvent
    public static void onRenderWorld(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;

        PoseStack ms = event.getPoseStack();
        ms.pushPose();
        SuperRenderTypeBuffer buffer = SuperRenderTypeBuffer.getInstance();
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
        if (!player.getPersistentData().getBoolean(TravelManager.TRAVEL_TAG)) return;

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
        if (!player.getPersistentData().getBoolean(TravelManager.TRAVEL_TAG)) return;

        event.getPoseStack().popPose();

    }
}
