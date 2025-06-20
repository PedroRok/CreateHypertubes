package com.pedrorok.hypertube.mixin.core;

import com.pedrorok.hypertube.managers.travel.TravelConstants;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Rok, Pedro Lucas nmm. Created on 19/06/2025
 * @project Create Hypertube
 */
@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;

        if (!entity.getPersistentData().getBoolean(TravelConstants.TRAVEL_TAG)) return;

        Vec3 velocity = new Vec3(entity.getDeltaMovement().x, entity.getDeltaMovement().y, entity.getDeltaMovement().z);

        if (!(velocity.lengthSqr() > 0.001D)) return;
        Vec3 lastMovementDirection = velocity.normalize();

        float yaw = (float) Math.toDegrees(Math.atan2(-lastMovementDirection.x, lastMovementDirection.z));
        float pitch = (float) Math.toDegrees(Math.atan2(-lastMovementDirection.y, Math.sqrt(lastMovementDirection.x * lastMovementDirection.x + lastMovementDirection.z * lastMovementDirection.z)));

        entity.setYRot(yaw);
        entity.setXRot(pitch);
    }
}
