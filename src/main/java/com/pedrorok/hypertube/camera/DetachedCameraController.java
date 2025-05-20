package com.pedrorok.hypertube.camera;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/**
 * @author Rok, Pedro Lucas nmm. Created on 22/04/2025
 * @project Create Hypertube
 */
public class DetachedCameraController {

    private static DetachedCameraController INSTANCE;

    public static DetachedCameraController get() {
        if (INSTANCE == null) {
            INSTANCE = new DetachedCameraController();
        }
        return INSTANCE;
    }

    @Getter
    @Setter
    private boolean detached = false;
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
    private static final double SMOOTHING_ROTATION = 0.01;

    private DetachedCameraController() {
    }

    public void setCameraRotation(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
        this.targetYaw = yaw;
        this.targetPitch = pitch;
    }

    public void setCameraPosition(Entity renderViewEntity) {
        Vec3 cameraPos = getRelativeCameraPos(renderViewEntity);
        this.currentPos = cameraPos;
        this.targetPos = cameraPos;
    }

    public void updateCameraRotation(float deltaYaw, float deltaPitch) {
        this.targetYaw += deltaYaw;
        this.targetPitch += deltaPitch;

        this.targetPitch = Mth.clamp(this.targetPitch, -90, 90);
    }

    private Vec3 getRelativeCameraPos(Entity renderViewEntity) {
        Vec3 deltaMovement = renderViewEntity.getDeltaMovement();
        return renderViewEntity
                .position()
                .subtract(deltaMovement.multiply(8, 8, 8))
                .add(0, 3, 0);
    }

    public void tickCamera(Entity renderViewEntity) {
        updateTargetPosition(getRelativeCameraPos(renderViewEntity));
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
}
