package com.pedrorok.hypertube.core.camera;

import net.minecraft.SharedConstants;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * @author Rok, Pedro Lucas nmm. Created on 29/07/2026
 * @project Create Hypertube
 */
@OnlyIn(Dist.CLIENT)
public class CameraSmoothing {

    public static final float REFERENCE_RATE = 60f;

    private static final float MAX_DELTA_SECONDS = 2f * SharedConstants.MILLIS_PER_TICK / 1000f;

    private CameraSmoothing() {
    }

    public static float clampDelta(float deltaSeconds) {
        return Mth.clamp(deltaSeconds, 0f, MAX_DELTA_SECONDS);
    }

    public static float steps(float deltaSeconds) {
        return clampDelta(deltaSeconds) * REFERENCE_RATE;
    }

    public static float factor(double factorPerStep, float deltaSeconds) {
        if (factorPerStep >= 1) return 1;
        return (float) (1 - Math.pow(1 - factorPerStep, steps(deltaSeconds)));
    }
}
