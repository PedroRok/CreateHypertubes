package com.pedrorok.hypertube.utils;

import net.minecraft.world.phys.Vec3;

/**
 * @author Rok, Pedro Lucas nmm. Created on 08/05/2025
 * @project Create Hypertube
 */
public class MathUtils {


    public static float getMediumSpeed(Vec3 velocity) {
        float x = Math.abs((float) velocity.x);
        float y = Math.abs((float) velocity.y);
        float z = Math.abs((float) velocity.z);
        float sum = x + y + z;
        return sum / 3;
    }
}
