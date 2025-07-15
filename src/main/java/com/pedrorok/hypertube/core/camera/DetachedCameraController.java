package com.pedrorok.hypertube.core.camera;

import com.pedrorok.hypertube.config.ClientConfig;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * @author Rok, Pedro Lucas nmm. Created on 22/04/2025
 * @project Create Hypertube
 */
@OnlyIn(Dist.CLIENT)
public class DetachedCameraController {

    private static DetachedCameraController INSTANCE;

    public static DetachedCameraController get() {
        if (INSTANCE == null) {
            INSTANCE = new DetachedCameraController();
        }
        return INSTANCE;
    }

    @Getter
    private float yaw = 0;
    @Getter
    private float pitch = 0;

    @Getter
    private Vec3 currentPos = Vec3.ZERO;

    private Vec3 targetPos = Vec3.ZERO;
    @Getter
    private float targetYaw = 0;
    @Getter
    private float targetPitch = 0;

    private static final double SMOOTHING = 0.1;
    private static final double SMOOTHING_ROTATION = 0.1;

    private float lastMouseMov = 0;

    @Getter
    @Setter
    private boolean detached = false;

    private DetachedCameraController() {
    }

    public void startCamera(Entity renderViewEntity) {
        Vec3 cameraPos = getRelativeCameraPos(renderViewEntity);
        this.currentPos = cameraPos;
        this.targetPos = cameraPos;
        this.lastMouseMov = 0;
        this.yaw = this.targetYaw = Mth.wrapDegrees(renderViewEntity.getYRot());
        this.pitch = this.targetPitch = 30;
    }

    public void updateCameraRotation(float deltaYaw, float deltaPitch, boolean isCamera) {
        this.targetYaw = Mth.wrapDegrees(this.targetYaw + deltaYaw);
        this.targetPitch = Mth.clamp(this.targetPitch + deltaPitch, -90, 90);

        if (lastMouseMov != 0) {
            lastMouseMov = Math.max(0, lastMouseMov - 0.015f);
        }
        if (isCamera && deltaYaw != 0) {
            lastMouseMov = 2;
        }
    }

    private float getCameraYaw(Vec3 entityPos, Vec3 cameraPos) {
        Vec3 cameraToPlayerNormal = cameraPos.subtract(entityPos).multiply(1, 0, 1).normalize();
        float yaw = (float) Math.toDegrees(Math.atan2(cameraToPlayerNormal.z, cameraToPlayerNormal.x)) + 90;
        yaw = Mth.wrapDegrees(yaw);
        return (((yaw - this.yaw + 540) % 360) - 180) * (1 - Math.min(lastMouseMov, 1));
    }

    private float getCameraPitch() {
        return (((30 - this.pitch + 540) % 360) - 180) * (1 - Math.min(lastMouseMov, 1));
    }

    private Vec3 getRelativeCameraPos(Entity renderViewEntity) {
        Vec3 dir = DetachedPlayerDirController.get().getDirection();
        return renderViewEntity
                .position()
                .subtract(dir.multiply(8, 8, 8))
                .add(0, 3, 0);
    }

    public void tickCamera(Entity renderViewEntity) {
        Vec3 entityPos = renderViewEntity.position();
        Vec3 relativeCameraPos = getRelativeCameraPos(renderViewEntity);

        updateCameraRotation(getCameraYaw(entityPos, relativeCameraPos) * 0.1f, getCameraPitch() * 0.1f, false);

        updateTargetPosition(relativeCameraPos);
        tickCameraPosRot();
    }

    public void updateTargetPosition(Vec3 pos) {
        this.targetPos = pos;
    }

    public void tickCameraPosRot() {
        this.currentPos = this.currentPos.lerp(this.targetPos, SMOOTHING);
        this.yaw = lerpAngle(this.yaw, this.targetYaw, (float) SMOOTHING_ROTATION);
        this.pitch = (float) Mth.lerp(SMOOTHING_ROTATION, this.pitch, this.targetPitch);
    }

    private float lerpAngle(float from, float to, float t) {
        float delta = Mth.wrapDegrees(to - from);
        return from + delta * t;
    }

    private static double lastMouseX = 0;
    private static double lastMouseY = 0;

    public static void cameraTick() {
        Minecraft mc = Minecraft.getInstance();
        if ((mc.options.getCameraType().isFirstPerson() && ClientConfig.get().ALLOW_FPV_INSIDE_TUBE.get())
            || mc.isPaused()
            || !mc.isWindowActive()
            || mc.screen != null)
            return;

        MouseHandler mouse = mc.mouseHandler;
        double dx = mouse.xpos() - lastMouseX;
        double dy = mouse.ypos() - lastMouseY;

        double sensitivity = mc.options.sensitivity().get();
        double factor = sensitivity * 0.3 + 0.1;
        factor = factor * factor * factor * 8.0;
        DetachedCameraController.get().updateCameraRotation((float) (dx * factor), (float) (dy * factor), true);
        lastMouseX = mc.mouseHandler.xpos();
        lastMouseY = mc.mouseHandler.ypos();
    }
}
