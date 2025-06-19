package com.pedrorok.hypertube.mixin.fix;

import com.pedrorok.hypertube.managers.travel.TravelManager;
import io.socol.betterthirdperson.api.CustomCameraManager;
import io.socol.betterthirdperson.api.adapter.IPlayerAdapter;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author Rok, Pedro Lucas nmm. Created on 18/06/2025
 * @project Create Hypertube
 */
@Mixin(value = CustomCameraManager.class, remap = false)
public class MixinCustomCameraManager {

    @Inject(method = "mustHaveCustomCamera", at = @At("HEAD"), cancellable = true)
    public void mustHaveCustomCamera(IPlayerAdapter player, CallbackInfoReturnable<Boolean> cir) {
        if (!TravelManager.hasHyperTubeData(Minecraft.getInstance().player)) return;
        cir.cancel();
        cir.setReturnValue(false);
    }

    @Inject(method = "hasCustomCamera", at = @At("HEAD"), cancellable = true)
    public void hasCustomCamera(CallbackInfoReturnable<Boolean> cir) {
        if (!TravelManager.hasHyperTubeData(Minecraft.getInstance().player)) return;
        cir.cancel();
        cir.setReturnValue(false);
    }
}
