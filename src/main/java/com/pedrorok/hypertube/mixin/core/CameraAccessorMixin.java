package com.pedrorok.hypertube.mixin.core;

import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * @author Rok, Pedro Lucas nmm. Created on 10/05/2025
 * @project Create Hypertube
 */
@Mixin(Camera.class)
public interface CameraAccessorMixin {

    @Invoker("setPosition")
    void createHypertube$callSetPosition(double x, double y, double z);

    @Invoker("setRotation")
    void createHypertube$callSetRotation(float yaw, float pitch);

    @Invoker("move")
    void createHypertube$callMove(float x, float y, float z);

    @Invoker("getMaxZoom")
    float createHypertube$callGetMaxZoom(float f);
}
