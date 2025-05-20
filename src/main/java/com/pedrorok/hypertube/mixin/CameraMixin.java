package com.pedrorok.hypertube.mixin;

import com.pedrorok.hypertube.camera.DetachedCameraController;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Rok, Pedro Lucas nmm. Created on 22/04/2025
 * @project Create Hypertube
 */
@Mixin(Camera.class)
public class CameraMixin {

    @Inject(method = "setup", at = @At("HEAD"), cancellable = true)
    private void onSetup(BlockGetter p_90576_, Entity renderViewEntity, boolean isFrontView, boolean p_90579_, float PartialTicks, CallbackInfo ci) {
        if (!DetachedCameraController.detached) return;
        DetachedCameraController.tickCamera(renderViewEntity);

        CameraAccessorMixin camera = (CameraAccessorMixin) this;
        camera.callSetPosition(DetachedCameraController.getCurrentPos());
        camera.callSetRotation(DetachedCameraController.yaw, DetachedCameraController.pitch);

        ci.cancel();
    }


}
