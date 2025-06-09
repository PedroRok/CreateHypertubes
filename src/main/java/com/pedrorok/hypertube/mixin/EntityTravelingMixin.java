package com.pedrorok.hypertube.mixin;

import com.pedrorok.hypertube.managers.TravelManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(Entity.class)
public class EntityTravelingMixin {

    @Inject(method = "isNoGravity", at = @At("HEAD"), cancellable = true)
    private void cancelLerpMotion(CallbackInfoReturnable<Boolean> cir) {
        if (!(((Entity) (Object) this) instanceof Player player)
            || !player.getPersistentData().getBoolean(TravelManager.TRAVEL_TAG)) return;
        cir.setReturnValue(true);
    }
}
