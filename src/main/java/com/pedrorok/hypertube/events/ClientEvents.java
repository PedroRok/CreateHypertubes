package com.pedrorok.hypertube.events;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.pedrorok.hypertube.camera.DetachedCameraController;
import com.pedrorok.hypertube.managers.TravelManager;
import com.pedrorok.hypertube.managers.placement.TubePlacement;
import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.render.DefaultSuperRenderTypeBuffer;
import net.createmod.catnip.render.SuperRenderTypeBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

/**
 * @author Rok, Pedro Lucas nmm. Created on 23/04/2025
 * @project Create Hypertube
 */
@EventBusSubscriber(Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void onTickPost(ClientTickEvent.Post event) {
        onTick();
    }

    private static void onTick() {
        if (!isGameActive()) return;
        TubePlacement.clientTick();
        tickPlayer();
    }

    private static void tickPlayer() {
        Player player = Minecraft.getInstance().player;
        // Check if the player has the pointing pose tag
        CompoundTag persistentData = player.getPersistentData();
        if (persistentData.getBoolean(TravelManager.TRAVEL_TAG)) {
            if (!player.isFallFlying())
                player.startFallFlying();
        } else if (persistentData.contains(TravelManager.TRAVEL_TAG)) {
            player.stopFallFlying();
            persistentData.remove(TravelManager.TRAVEL_TAG);
        }
    }


    @SubscribeEvent
    public static void onRenderWorld(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES)
            return;

        PoseStack ms = event.getPoseStack();
        ms.pushPose();
        SuperRenderTypeBuffer buffer = DefaultSuperRenderTypeBuffer.getInstance();
        Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera()
                .getPosition();

        TubePlacement.drawCustomBlockSelection(ms, buffer, camera);

        buffer.draw();
        RenderSystem.enableCull();
        ms.popPose();
    }

    protected static boolean isGameActive() {
        return !(Minecraft.getInstance().level == null || Minecraft.getInstance().player == null);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {

        Minecraft mc = Minecraft.getInstance();
        if (!DetachedCameraController.detached || mc.isPaused() || !mc.isWindowActive()) return;

        MouseHandler mouse = mc.mouseHandler;

        double dx = mouse.getXVelocity();
        double dy = mouse.getYVelocity();

        double sensitivity = mc.options.sensitivity().get();
        double factor = sensitivity * 0.6 + 0.2;
        factor = factor * factor * factor * 8.0;
        DetachedCameraController.updateCameraRotation((float) (dx * factor), (float) (dy * factor));
    }

}
