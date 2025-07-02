package com.pedrorok.hypertube.mixin.core;

import com.pedrorok.hypertube.core.travel.TravelConstants;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.extensions.IPlayerExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author Rok, Pedro Lucas nmm. Created on 01/07/2025
 * @project Create Hypertube
 */
@Mixin(IPlayerExtension.class)
public interface IPlayerExtensionMixin {

    @Inject(method = "mayFly", at = @At("HEAD"), cancellable = true)
    default void createHypertube$mayFly(CallbackInfoReturnable<Boolean> cir) {
        Player player = (Player) (Object) this;
        if (!player.getPersistentData().getBoolean(TravelConstants.TRAVEL_TAG)) return;
        cir.setReturnValue(false);
    }
}
