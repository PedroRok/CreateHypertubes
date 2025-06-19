package com.pedrorok.hypertube.mixin.fix;

import com.pedrorok.hypertube.managers.travel.TravelManager;
import io.socol.betterthirdperson.api.CustomCamera;
import io.socol.betterthirdperson.api.TickPhase;
import io.socol.betterthirdperson.api.adapter.IClientAdapter;
import io.socol.betterthirdperson.api.adapter.IPlayerAdapter;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author Rok, Pedro Lucas nmm. Created on 18/06/2025
 * @project Create Hypertube
 */
@Mixin(value = CustomCamera.class, remap = false)
public class MixinBetterThirdPerson {

    @Inject(method = "setup", at = @At("HEAD"), cancellable = true)
    public void setup(IClientAdapter client, IPlayerAdapter player, float partialTicks, CallbackInfo ci) {
        if (!TravelManager.hasHyperTubeData(Minecraft.getInstance().player)) return;
        ci.cancel();
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    public void tick(TickPhase phase, IPlayerAdapter player, CallbackInfo ci) {
        if (!TravelManager.hasHyperTubeData(Minecraft.getInstance().player)) return;
        ci.cancel();
    }
}
