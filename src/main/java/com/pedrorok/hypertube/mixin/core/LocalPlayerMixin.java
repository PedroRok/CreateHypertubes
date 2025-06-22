package com.pedrorok.hypertube.mixin.core;

import com.pedrorok.hypertube.managers.travel.TravelConstants;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author Rok, Pedro Lucas nmm. Created on 21/06/2025
 * @project Create Hypertube
 */
@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {

    @Inject(method = "isShiftKeyDown", at = @At("HEAD"), cancellable = true)
    private void cancelShiftKeyDown(CallbackInfoReturnable<Boolean> cir) {
        LocalPlayer player = (LocalPlayer) (Object) this;
        if (!player.getPersistentData().getBoolean(TravelConstants.TRAVEL_TAG)) return;
        cir.setReturnValue(false);
    }
}
