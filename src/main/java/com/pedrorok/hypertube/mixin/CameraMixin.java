package com.pedrorok.hypertube.mixin;

import com.pedrorok.hypertube.managers.camera.DetachedCameraController;
import com.pedrorok.hypertube.config.ClientConfig;
import com.pedrorok.hypertube.managers.TravelManager;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.neoforged.neoforge.client.ClientHooks;
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

    @Unique
    public void createHypertube$setDetachedExternal(boolean newDetached) {
        this.detached = newDetached;
    }

    @Inject(method = "setup", at = @At("HEAD"), cancellable = true)
    private void onSetup(BlockGetter p_90576_, Entity renderViewEntity, boolean isFrontView, boolean flipped, float PartialTicks, CallbackInfo ci) {
        Options options = Minecraft.getInstance().options;
        Player player = Minecraft.getInstance().player;
        if (renderViewEntity != player) return;
        if (!TravelManager.hasHyperTubeData(renderViewEntity) || (
                options.getCameraType().isFirstPerson() && ClientConfig.get().ALLOW_FPV_INSIDE_TUBE.get())) {

            if (DetachedCameraController.get().isDetached()) {
                renderViewEntity.setYRot(DetachedCameraController.get().getYaw());
                renderViewEntity.setXRot(DetachedCameraController.get().getPitch());
            }
            DetachedCameraController.get().setDetached(false);
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
        DetachedCameraController.get().tickCamera(renderViewEntity);

        camera.callSetRotation(DetachedCameraController.get().getYaw() * (flipped ? -1 : 1), DetachedCameraController.get().getPitch());


        camera.callSetPosition(
                Mth.lerp(PartialTicks, renderViewEntity.xo, renderViewEntity.getX()),
                Mth.lerp(PartialTicks, renderViewEntity.yo, renderViewEntity.getY()),
                Mth.lerp(PartialTicks, renderViewEntity.zo, renderViewEntity.getZ()));

        float f;
        if (renderViewEntity instanceof LivingEntity livingentity) {
            f = livingentity.getScale();
        } else {
            f = 1.0F;
        }
        camera.callMove(-camera.callGetMaxZoom(ClientHooks.getDetachedCameraDistance(cameraObj, flipped, f, 4.0F) * f), 0.0F, 0.0F);

        ci.cancel();
    }
}
