package com.pedrorok.hypertube.camera;

import net.minecraft.util.Mth;

public class DetachedCameraController {
    public static boolean detached = false;
    public static float yaw = 0;
    public static float pitch = 0;

    public static void updateCameraRotation(float deltaYaw, float deltaPitch) {
        yaw += deltaYaw;
        pitch += deltaPitch;

        // Clamp pitch
        pitch = Mth.clamp(pitch, -90, 90);
    }
}
