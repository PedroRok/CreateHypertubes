package com.pedrorok.hypertube.mixin;

import com.pedrorok.hypertube.camera.DetachedCameraController;
import com.simibubi.create.content.trains.CameraDistanceModifier;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.ClientHooks;
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
    private void onSetup(BlockGetter p_90576_, Entity renderViewEntity, boolean isFrontView, boolean flipped, float PartialTicks, CallbackInfo ci) {
        if (!DetachedCameraController.get().isDetached()) return;
        DetachedCameraController.get().tickCamera(renderViewEntity);
        Camera cameraObj = (Camera) (Object) this;
        CameraAccessorMixin camera = (CameraAccessorMixin) cameraObj;
        camera.callSetRotation(DetachedCameraController.get().getYaw() * (flipped ? -1:1), DetachedCameraController.get().getPitch());


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
