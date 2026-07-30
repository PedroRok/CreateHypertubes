package com.pedrorok.hypertube.mixin.core;

import com.pedrorok.hypertube.config.ClientConfig;
import com.pedrorok.hypertube.core.camera.CameraSmoothing;
import com.pedrorok.hypertube.core.camera.DetachedCameraController;
import com.pedrorok.hypertube.core.camera.DetachedPlayerDirController;
import com.pedrorok.hypertube.core.travel.TravelManager;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
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
@SuppressWarnings("ALL")
@Mixin(Camera.class)
public class CameraMixin {

    @Shadow
    private boolean detached;

    @Shadow
    private Entity entity;

    @Unique
    private long createHypertube$lastTickTime = 0;


    @Unique
    public void createHypertube$setDetachedExternal(boolean newDetached) {
        this.detached = newDetached;
    }

    @Inject(method = "setup", at = @At("HEAD"), cancellable = true)
    private void createHypertube$onSetup(BlockGetter p_90576_, Entity renderViewEntity, boolean isFrontView, boolean flipped, float PartialTicks, CallbackInfo ci) {
        if (renderViewEntity == null) {
            return;
        }
        Options options = Minecraft.getInstance().options;
        Player player = Minecraft.getInstance().player;
        if (renderViewEntity != player) return;

        DetachedCameraController ctrl = DetachedCameraController.get();
        boolean inTube = TravelManager.hasHyperTubeData(renderViewEntity);
        boolean allowFpv = ClientConfig.get().ALLOW_FPV_INSIDE_TUBE.get();
        boolean fpvInside = options.getCameraType().isFirstPerson() && allowFpv;

        if (!inTube && !ctrl.isDetached()) {
            DetachedPlayerDirController.get().setDetached(false);
            return;
        }

        if (fpvInside) {
            ctrl.setDetached(false);
            ctrl.snapTransition(0f);
            this.createHypertube$setDetachedExternal(false);
            if (!inTube) {
                DetachedPlayerDirController.get().setDetached(false);
            }
            return;
        }

        if (inTube) {
            if (!ctrl.isDetached()) {
                if (!allowFpv) {
                    options.setCameraType(CameraType.THIRD_PERSON_BACK);
                }
                ctrl.startCamera(renderViewEntity);
                ctrl.snapTransition(0.2f);
                ctrl.setDetached(true);
                this.createHypertube$setDetachedExternal(true);
            }
            ctrl.setTransitionTarget(1f);
        } else {
            ctrl.setTransitionTarget(0f);
            // the travel is over: the player aims again right away, only the camera keeps easing back
            DetachedPlayerDirController.get().setDetached(false);
            if (ctrl.getTransition() <= 0.2f) {
                ctrl.snapTransition(0f);
                this.createHypertube$setDetachedExternal(false);
                if (!allowFpv) {
                    options.setCameraType(CameraType.FIRST_PERSON);
                }
                ctrl.setDetached(false);
                return;
            }
            if (!allowFpv) {
                options.setCameraType(CameraType.THIRD_PERSON_BACK);
            }
        }

        Camera cameraObj = (Camera) (Object) this;
        CameraAccessorMixin camera = (CameraAccessorMixin) cameraObj;

        long currentTime = System.nanoTime();
        float deltaSeconds = createHypertube$lastTickTime == 0
                ? 1 / CameraSmoothing.REFERENCE_RATE
                : (currentTime - createHypertube$lastTickTime) / 1000000000f;
        createHypertube$lastTickTime = currentTime;

        ctrl.tickCamera(renderViewEntity, deltaSeconds);
        createHypertube$doTick(player);

        ctrl.tickTransition();
        float eased = ctrl.getEasedTransition();

        float firstPersonYaw = renderViewEntity.getViewYRot(PartialTicks);
        float firstPersonPitch = renderViewEntity.getViewXRot(PartialTicks);
        float orbitYaw = ctrl.getYaw() * (flipped ? -1 : 1);
        float orbitPitch = ctrl.getPitch();
        camera.callSetRotation(
                Mth.rotLerp(eased, firstPersonYaw, orbitYaw),
                Mth.lerp(eased, firstPersonPitch, orbitPitch));

        double eyeAdd = renderViewEntity.getEyeHeight() * (1f - eased);
        camera.callSetPosition(
                Mth.lerp(PartialTicks, renderViewEntity.xo, renderViewEntity.getX()),
                Mth.lerp(PartialTicks, renderViewEntity.yo, renderViewEntity.getY()) + eyeAdd,
                Mth.lerp(PartialTicks, renderViewEntity.zo, renderViewEntity.getZ()));

        double zoom = camera.callGetMaxZoom(4.0D);
        camera.callMove(-zoom * eased, 0.0F, 0.0F);

        ci.cancel();
    }


    @Unique
    private void createHypertube$doTick(Player player) {
        if (!(player.getXRot() > 85) && !(player.getXRot() < -85)) {
            DetachedCameraController.get().setCheckDirection(null);
            DetachedCameraController.get().setCameraHorizontalCompensation(0);
            return;
        }
        BlockPos playerBlockPos = new BlockPos((int) player.position().x, (int) player.position().y, (int) player.position().z);
        BlockPos relative = playerBlockPos.relative(Direction.EAST);
        boolean air = Minecraft.getInstance().level.getBlockState(relative).isAir();

        if (!air) {
            BlockPos relativeBlock = playerBlockPos.relative(Direction.WEST);
            boolean relativeAir = Minecraft.getInstance().level.getBlockState(relativeBlock).isAir();
            if (relativeAir) {
                DetachedCameraController.get().setCameraHorizontalCompensation(180);
                return;
            }
            relativeBlock = playerBlockPos.relative(Direction.NORTH);
            relativeAir = Minecraft.getInstance().level.getBlockState(relativeBlock).isAir();
            if (relativeAir) {
                DetachedCameraController.get().setCameraHorizontalCompensation(-90);
                return;
            }
            relativeBlock = playerBlockPos.relative(Direction.SOUTH);
            relativeAir = Minecraft.getInstance().level.getBlockState(relativeBlock).isAir();
            if (!relativeAir) return;
            DetachedCameraController.get().setCameraHorizontalCompensation(90);
            return;
        }
        DetachedCameraController.get().setCameraHorizontalCompensation(0);
        DetachedCameraController.get().setCheckDirection(null);
    }
}
