package com.pedrorok.hypertube.core.camera;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * @author Rok, Pedro Lucas nmm. Created on 27/06/2025
 * @project Create Hypertube
 */
@Getter
@OnlyIn(Dist.CLIENT)
public class DetachedPlayerDirController {

    private static DetachedPlayerDirController INSTANCE;

    public static DetachedPlayerDirController get() {
        if (INSTANCE == null) {
            INSTANCE = new DetachedPlayerDirController();
        }
        return INSTANCE;
    }

    private float yaw = 0;
    private float pitch = 0;

    private float targetYaw = 0;
    private float targetPitch = 0;

    private static final double SMOOTHING_ROTATION = 0.75;

    @Setter
    private boolean detached = false;

    public void updateRotation(float newYaw, float newPitch) {
        if (!detached) {
            yaw = newYaw;
            pitch = newPitch;
            return;
        }
        this.targetYaw = newYaw;
        this.targetPitch = newPitch;
    }

    public void tickPlayerDirection() {
        if (!detached) return;
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        this.yaw = lerpAngle(this.yaw, this.targetYaw, (float) SMOOTHING_ROTATION);
        this.pitch = (float) Mth.lerp(SMOOTHING_ROTATION, this.pitch, this.targetPitch);
        player.setYRot(this.yaw);
        player.setXRot(this.pitch);
    }

    private float lerpAngle(float from, float to, float t) {
        float delta = Mth.wrapDegrees(to - from);
        return from + delta * t;
    }

    public static void tickPlayer() {
        get().tickPlayerDirection();
    }


    public Vec3 getDirection() {
        return new Vec3(
                -Mth.sin((float) Math.toRadians(yaw)) * Mth.cos((float) Math.toRadians(pitch)),
                -Mth.sin((float) Math.toRadians(pitch)),
                Mth.cos((float) Math.toRadians(yaw)) * Mth.cos((float) Math.toRadians(pitch))
        );
    }
}