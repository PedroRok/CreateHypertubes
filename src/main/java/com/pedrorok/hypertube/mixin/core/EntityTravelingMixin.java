package com.pedrorok.hypertube.mixin.core;

import com.pedrorok.hypertube.core.travel.TravelConstants;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(Entity.class)
public class EntityTravelingMixin {

    @Inject(method = "getGravity", at = @At("HEAD"), cancellable = true)
    private void createHypertube$cancelLerpMotion(CallbackInfoReturnable<Double> cir) {
        if (!(((Entity) (Object) this) instanceof LivingEntity entity)
            || !entity.getPersistentData().getBoolean(TravelConstants.TRAVEL_TAG)) return;
        cir.setReturnValue(0.0D);
    }

    @Inject(method = "getPose", at = @At("HEAD"), cancellable = true)
    private void createHypertube$cancelPose(CallbackInfoReturnable<Pose> cir) {
        if (!(((Entity) (Object) this) instanceof LivingEntity player)
            || !player.getPersistentData().getBoolean(TravelConstants.TRAVEL_TAG)) return;
        cir.setReturnValue(Pose.STANDING);
    }
}
