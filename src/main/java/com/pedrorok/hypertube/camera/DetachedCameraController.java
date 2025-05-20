package com.pedrorok.hypertube.camera;

import lombok.Getter;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/**
 * @author Rok, Pedro Lucas nmm. Created on 22/04/2025
 * @project Create Hypertube
 */
public class DetachedCameraController {
    public static boolean detached = false;
    public static float yaw = 0;
    public static float pitch = 0;

    @Getter
    private static Vec3 currentPos = Vec3.ZERO;

    private static Vec3 targetPos = Vec3.ZERO;
    @Getter
    private static float targetYaw = 0;
    @Getter
    private static float targetPitch = 0;
    private static final double SMOOTHING = 0.1;
    private static final double SMOOTHING_ROTATION = 0.01;

    public static void setCameraRotation(float yaw, float pitch) {
        DetachedCameraController.yaw = yaw;
        DetachedCameraController.pitch = pitch;
        targetYaw = yaw;
        targetPitch = pitch;
    }

    public static void setCameraPosition(Entity renderViewEntity) {
        Vec3 cameraPos = getRelativeCameraPos(renderViewEntity);
        currentPos = cameraPos;
        targetPos = cameraPos;
    }

    public static void updateCameraRotation(float deltaYaw, float deltaPitch) {
        targetYaw += deltaYaw;
        targetPitch += deltaPitch;

        targetPitch = Mth.clamp(targetPitch, -90, 90);
    }

    private static Vec3 getRelativeCameraPos(Entity renderViewEntity) {
        Vec3 deltaMovement = renderViewEntity.getDeltaMovement();
        return renderViewEntity
                .position()
                .subtract(deltaMovement.multiply(8,8,8))
                .add(0,3,0);
    }


    public static void tickCamera(Entity renderViewEntity) {
        DetachedCameraController.updateTargetPosition(getRelativeCameraPos(renderViewEntity));
        tickCameraPosRot();
    }

    public static void updateTargetPosition(Vec3 pos) {
        targetPos = pos;
    }

    public static void tickCameraPosRot() {
        currentPos = currentPos.lerp(targetPos, SMOOTHING);
        yaw = (float) Mth.lerp(SMOOTHING_ROTATION, yaw, targetYaw);
        pitch = (float) Mth.lerp(SMOOTHING_ROTATION, pitch, targetPitch);
    }

}
