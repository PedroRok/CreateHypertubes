package com.pedrorok.hypertube.mixin;

import com.pedrorok.hypertube.managers.travel.TravelConstants;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Rok, Pedro Lucas nmm. Created on 22/04/2025
 * @project Create Hypertube
 */
@Mixin(Player.class)
public abstract class PlayerMovementMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        Player player = (Player) (Object) this;

        if (!player.getPersistentData().getBoolean(TravelConstants.TRAVEL_TAG)) return;

        Vec3 velocity = new Vec3(player.getDeltaMovement().x, player.getDeltaMovement().y, player.getDeltaMovement().z);

        if (!(velocity.lengthSqr() > 0.001D)) return;
        Vec3 lastMovementDirection = velocity.normalize();

        float yaw = (float) Math.toDegrees(Math.atan2(-lastMovementDirection.x, lastMovementDirection.z));
        float pitch = (float) Math.toDegrees(Math.atan2(-lastMovementDirection.y, Math.sqrt(lastMovementDirection.x * lastMovementDirection.x + lastMovementDirection.z * lastMovementDirection.z)));

        player.setYRot(yaw);
        player.setXRot(pitch);
    }

    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    private void onTravel(Vec3 pTravelVector, CallbackInfo ci) {
        Player player = (Player) (Object) this;

        if (!player.getPersistentData().getBoolean(TravelConstants.TRAVEL_TAG)) return;
        ci.cancel();
        player.resetFallDistance();
        player.move(MoverType.SELF, player.getDeltaMovement());
    }

}
