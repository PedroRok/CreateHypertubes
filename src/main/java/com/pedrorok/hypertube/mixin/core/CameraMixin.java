package com.pedrorok.hypertube.mixin.core;

import com.pedrorok.hypertube.core.camera.DetachedCameraController;
import com.pedrorok.hypertube.config.ClientConfig;
import com.pedrorok.hypertube.core.camera.DetachedPlayerDirController;
import com.pedrorok.hypertube.core.travel.TravelManager;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Rok, Pedro Lucas nmm. Created on 22/04/2025
 * @project Create Hypertube
 */
@Mixin(Camera.class)
public class CameraMixin {

    @Shadow
    private boolean detached;

    @Shadow private Entity entity;

    // FPS CONTROL
    @Unique
    private long createHypertube$lastTickTime = 0;
    @Unique
    private static final long createHypertube$TICK_INTERVAL_NS = 1_000_000_000L / 60;


    @Unique
    public void createHypertube$setDetachedExternal(boolean newDetached) {
        this.detached = newDetached;
    }

    @Inject(method = "setup", at = @At("HEAD"), cancellable = true)
    private void onSetup(BlockGetter p_90576_, Entity renderViewEntity, boolean isFrontView, boolean flipped, float PartialTicks, CallbackInfo ci) {
        Options options = Minecraft.getInstance().options;
        Player player = Minecraft.getInstance().player;
        if (renderViewEntity != player) return;
        boolean hasHypertubeData = !TravelManager.hasHyperTubeData(renderViewEntity);
        if (hasHypertubeData || (
                options.getCameraType().isFirstPerson() && ClientConfig.get().ALLOW_FPV_INSIDE_TUBE.get())) {
            DetachedCameraController.get().setDetached(false);
            if (hasHypertubeData) {
                DetachedPlayerDirController.get().setDetached(false);
            }
            return;
        }

        if (!ClientConfig.get().ALLOW_FPV_INSIDE_TUBE.get()){
            options.setCameraType(CameraType.THIRD_PERSON_BACK);
        }

        Camera cameraObj = (Camera) (Object) this;
        CameraAccessorMixin camera = (CameraAccessorMixin) cameraObj;

        if (!DetachedCameraController.get().isDetached()) {
            DetachedCameraController.get().startCamera(renderViewEntity);
            DetachedCameraController.get().setDetached(true);
            this.createHypertube$setDetachedExternal(true);
        }
        long currentTime = System.nanoTime();
        if (currentTime - createHypertube$lastTickTime >= createHypertube$TICK_INTERVAL_NS) {
            DetachedCameraController.get().tickCamera(renderViewEntity);
            createHypertube$lastTickTime = currentTime;
        }

        camera.callSetRotation(DetachedCameraController.get().getYaw() * (flipped ? -1 : 1), DetachedCameraController.get().getPitch());


        camera.callSetPosition(
                Mth.lerp(PartialTicks, renderViewEntity.xo, renderViewEntity.getX()),
                Mth.lerp(PartialTicks, renderViewEntity.yo, renderViewEntity.getY()),
                Mth.lerp(PartialTicks, renderViewEntity.zo, renderViewEntity.getZ()));

        camera.callMove(-camera.callGetMaxZoom(4.0F), 0.0F, 0.0F);

        ci.cancel();
    }
}
