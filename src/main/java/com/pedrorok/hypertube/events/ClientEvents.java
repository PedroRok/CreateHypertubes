package com.pedrorok.hypertube.events;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.pedrorok.hypertube.core.camera.DetachedCameraController;
import com.pedrorok.hypertube.core.camera.DetachedPlayerDirController;
import com.pedrorok.hypertube.core.placement.TubePlacement;
import com.pedrorok.hypertube.core.sound.TubeSoundManager;
import com.pedrorok.hypertube.core.travel.TravelConstants;
import net.createmod.catnip.render.DefaultSuperRenderTypeBuffer;
import net.createmod.catnip.render.SuperRenderTypeBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;

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
    public static void renderFrame(RenderFrameEvent.Pre event) {
        DetachedPlayerDirController.tickPlayer();
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
    public static void onRenderEntity(RenderLivingEvent.Pre event) {
        LivingEntity entity = event.getEntity();
        if (!entity.getPersistentData().getBoolean(TravelConstants.TRAVEL_TAG)) return;
        PoseStack poseStack = event.getPoseStack();
        TravelConstants.Client.ENTITIES_RENDER
                .get(entity.getType())
                .renderEntityOnTube()
                .accept(entity, poseStack);
    }

    @SubscribeEvent
    public static void onRenderEntityPost(RenderLivingEvent.Post event) {
        LivingEntity entity = event.getEntity();
        if (!entity.getPersistentData().getBoolean(TravelConstants.TRAVEL_TAG)) return;

        event.getPoseStack().popPose();

    }
}
