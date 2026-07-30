package com.pedrorok.hypertube.events;

import com.jozufozu.flywheel.util.AnimationTickHolder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.pedrorok.hypertube.client.TubePathOutline;
import com.pedrorok.hypertube.core.camera.CameraSmoothing;
import com.pedrorok.hypertube.core.travel.TravellerEntity;
import com.pedrorok.hypertube.core.travel.client.ClientTravelPathMover;
import com.pedrorok.hypertube.core.travel.client.ClientTravelPathRender;
import com.pedrorok.hypertube.utils.TubePulseRenderer;
import com.simibubi.create.foundation.render.SuperRenderTypeBuffer;
import com.pedrorok.hypertube.client.TubePathOutline;
import com.pedrorok.hypertube.core.camera.DetachedCameraController;
import com.pedrorok.hypertube.core.camera.DetachedPlayerDirController;
import com.pedrorok.hypertube.core.escape.TubeEscapeHandler;
import com.pedrorok.hypertube.core.placement.TubePlacement;
import com.pedrorok.hypertube.core.sound.TubeSoundManager;
import com.pedrorok.hypertube.core.travel.TravelConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderHighlightEvent;
import net.minecraftforge.client.event.RenderHighlightEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * @author Rok, Pedro Lucas nmm. Created on 23/04/2025
 * @project Create Hypertube
 */
@Mod.EventBusSubscriber(Dist.CLIENT)
public class ClientEvents {

    private static long lastFrameTime = 0;

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

    @SubscribeEvent
    public static void onRenderHighlight(RenderHighlightEvent.Block event) {
        TubePathOutline.renderTubeOutline(event);
    }

    private static void onTick(boolean isPreEvent) {
        if (!isGameActive()) return;

        if (isPreEvent) {
            TubeSoundManager.tickClientPlayerSounds();
            TubeEscapeHandler.onClientTick();
            return;
        }
        TubePlacement.clientTick();
        DetachedCameraController.tickCamera();
        ClientTravelPathMover.onClientTick();
    }

    @SubscribeEvent
    public static void renderFrame(TickEvent.RenderTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        float partialTick = event.renderTickTime;

        long currentTime = System.nanoTime();
        float deltaSeconds = lastFrameTime == 0 ? 1 / CameraSmoothing.REFERENCE_RATE : (currentTime - lastFrameTime) / 1_000_000_000f;
        lastFrameTime = currentTime;

        DetachedPlayerDirController.tickPlayer(deltaSeconds);

        ClientTravelPathMover.onRenderTick(partialTick);
    }

    @SubscribeEvent
    public static void afterRenderOverlayLayer(RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay() == VanillaGuiOverlay.CROSSHAIR.type()) {
            ClientTravelPathRender.renderOverlay(event.getGuiGraphics(), AnimationTickHolder.getPartialTicks());
        }
        if (event.getOverlay() == VanillaGuiOverlay.HOTBAR.type()) {
            TubeEscapeHandler.onRenderGuiOverlay(event.getGuiGraphics(), event.getPartialTick());
        }
    }

    @SubscribeEvent
    public static void onRenderWorld(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            PoseStack ms = event.getPoseStack();
            ms.pushPose();
            SuperRenderTypeBuffer buffer = SuperRenderTypeBuffer.getInstance();
            Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();

            TubePlacement.drawCustomBlockSelection(ms, buffer, camera);

            buffer.draw();
            RenderSystem.enableCull();
            ms.popPose();
        }
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            TubePulseRenderer.onRenderLevelStage(event.getPoseStack(), event.getPartialTick(), event.getCamera());
        }
    }

    protected static boolean isGameActive() {
        return !(Minecraft.getInstance().level == null || Minecraft.getInstance().player == null);
    }

    @SubscribeEvent
    public static void onRenderEntity(RenderLivingEvent.Pre event) {
        LivingEntity entity = event.getEntity();
        if (!entity.getPersistentData().getBoolean(TravelConstants.TRAVEL_TAG)) return;
        PoseStack poseStack = event.getPoseStack();
        TravellerEntity travellerEntity = TravelConstants.Client.ENTITIES_RENDER
                .get(entity.getType());
        if (travellerEntity == null) travellerEntity = TravellerEntity.ofAny(0.5f);
        travellerEntity
                .renderEntityOnTube()
                .accept(entity, poseStack);
        //TravelConstants.Client.ENTITIES_RENDER
        //        .get(entity.getType())
        //        .renderEntityOnTube()
        //        .accept(entity, poseStack);
    }

    @SubscribeEvent
    public static void onRenderEntityPost(RenderLivingEvent.Post event) {
        LivingEntity entity = event.getEntity();
        if (!entity.getPersistentData().getBoolean(TravelConstants.TRAVEL_TAG)) return;
        event.getPoseStack().popPose();
    }
}
