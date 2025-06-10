package com.pedrorok.hypertube.camera;

import lombok.Getter;
import lombok.Setter;
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
        this.yaw = this.targetYaw = renderViewEntity.getYRot();
        this.pitch = this.targetPitch = 30;
    }

    public void updateCameraRotation(float deltaYaw, float deltaPitch, boolean isCamera) {
        this.targetYaw += deltaYaw;
        this.targetPitch += deltaPitch;


        if (lastMouseMov != 0) {
            lastMouseMov = Math.max(0, lastMouseMov - 0.02f);
        }
        if (isCamera && deltaYaw != 0) {
            lastMouseMov = 2;
        }

        this.targetPitch = Mth.clamp(this.targetPitch, -90, 90);
    }

    private float getCameraYaw(Vec3 entityPos, Vec3 cameraPos) {
        Vec3 cameraToPlayerNormal = cameraPos.subtract(entityPos).multiply(1, 0, 1).normalize();
        float yaw = (float) Math.toDegrees(Math.atan2(cameraToPlayerNormal.z, cameraToPlayerNormal.x)) + 90;
        return (((yaw - this.yaw + 540) % 360) - 180) * (1 - Math.min(lastMouseMov, 1));
    }

    private float getCameraPitch() {
        // targe 30 degress ignoring player pos, only stay in 30 getting the actual pitch
        return (((30 - this.pitch + 540) % 360) - 180) * (1 - Math.min(lastMouseMov, 1));
    }

    private Vec3 getRelativeCameraPos(Entity renderViewEntity) {
        Vec3 deltaMovement = renderViewEntity.getDeltaMovement();
        return renderViewEntity
                .position()
                .subtract(deltaMovement.multiply(8, 8, 8))
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
        this.yaw = (float) Mth.lerp(SMOOTHING_ROTATION, this.yaw, this.targetYaw);
        this.pitch = (float) Mth.lerp(SMOOTHING_ROTATION, this.pitch, this.targetPitch);
    }


    private static double lastMouseX = 0;
    private static double lastMouseY = 0;

    public static void cameraTick() {
        Minecraft mc = Minecraft.getInstance();
        if ((mc.options.getCameraType().isFirstPerson() && ClientConfig.get().ALLOW_FPV_INSIDE_TUBE.get())
            || mc.isPaused()
            || !mc.isWindowActive())
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
