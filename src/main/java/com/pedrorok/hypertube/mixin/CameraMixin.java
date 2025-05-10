package com.pedrorok.hypertube.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.pedrorok.hypertube.camera.DetachedCameraController;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(Camera.class)
public class CameraMixin {

    @Inject(method = "setup", at = @At("HEAD"), cancellable = true)
    private void onSetup(BlockGetter p_90576_, Entity renderViewEntity, boolean isFrontView, boolean p_90579_, float PartialTicks, CallbackInfo ci) {
        if (!DetachedCameraController.detached) return;
        Camera camera = (Camera)(Object)this;

        Vec3 deltaMovement = renderViewEntity.getDeltaMovement();

        Vec3 pos = renderViewEntity.position().subtract(deltaMovement.multiply(8,8,8));
        // Set camera to follow player's position
        ((CameraAccessorMixin) camera).callSetPosition(pos.x, renderViewEntity.getEyeY() + 3, pos.z);

        // Apply custom pitch and yaw
        ((CameraAccessorMixin) camera).callSetRotation(DetachedCameraController.yaw, DetachedCameraController.pitch);

        // Cancel default behavior
        ci.cancel();
    }


}
