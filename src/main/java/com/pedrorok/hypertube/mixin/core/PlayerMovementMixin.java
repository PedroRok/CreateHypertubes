package com.pedrorok.hypertube.mixin.core;

import com.pedrorok.hypertube.core.travel.TravelConstants;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Rok, Pedro Lucas nmm. Created on 22/04/2025
 * @project Create Hypertube
 */
@Mixin(Player.class)
public abstract class PlayerMovementMixin {

    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    private void onTravel(Vec3 pTravelVector, CallbackInfo ci) {
        Player player = (Player) (Object) this;

        if (!player.getPersistentData().getBoolean(TravelConstants.TRAVEL_TAG)) return;
        ci.cancel();
        player.resetFallDistance();
        player.move(MoverType.SELF, player.getDeltaMovement());
    }
}
