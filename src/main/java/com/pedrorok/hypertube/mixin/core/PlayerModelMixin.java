package com.pedrorok.hypertube.mixin.core;

import com.pedrorok.hypertube.core.travel.TravelConstants;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Rok, Pedro Lucas nmm. Created on 23/05/2025
 * @project Create Hypertube
 */
@Mixin(value = HumanoidModel.class, priority = 1001)
public abstract class PlayerModelMixin {

    @Inject(method = "setupAnim*", at = @At("RETURN"), cancellable = true, order = 1001)
    private void onSetupAnim(LivingEntity entity, float limbSwing, float limbSwingAmount,
                             float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        if (!entity.getPersistentData().getBoolean(TravelConstants.TRAVEL_TAG)) return;


        HumanoidModel<?> model = (HumanoidModel<?>) (Object) this;
        boolean isPlayerModel = model instanceof PlayerModel;
        PlayerModel<?> playerModel = isPlayerModel ? (PlayerModel<?>) model : null;

        model.rightArm.xRot = 0;
        model.rightArm.yRot = 0;
        model.rightArm.zRot = 0;

        model.leftArm.xRot = 0;
        model.leftArm.yRot = 0;
        model.leftArm.zRot = 0;

        model.rightLeg.xRot = 0;
        model.rightLeg.yRot = 0;
        model.rightLeg.zRot = 0;


        model.leftLeg.xRot = 0;
        model.leftLeg.yRot = 0;
        model.leftLeg.zRot = 0;


        model.body.xRot = 0;
        model.body.yRot = 0;
        model.body.zRot = 0;

        model.head.xRot = -1.2F;
        model.head.yRot = 0;
        model.head.zRot = 0;

        // Fixing issue #6
        model.hat.xRot = -1.2F;
        model.hat.yRot = 0;
        model.hat.zRot = 0;


        if (isPlayerModel) {
            playerModel.rightSleeve.xRot = 0;

            playerModel.rightSleeve.yRot = 0;
            playerModel.rightSleeve.zRot = 0;

            playerModel.leftSleeve.xRot = 0;
            playerModel.leftSleeve.yRot = 0;
            playerModel.leftSleeve.zRot = 0;

            playerModel.rightPants.xRot = 0;
            playerModel.rightPants.yRot = 0;
            playerModel.rightPants.zRot = 0;

            playerModel.leftPants.xRot = 0;
            playerModel.leftPants.yRot = 0;
            playerModel.leftPants.zRot = 0;

            playerModel.jacket.xRot = 0;
            playerModel.jacket.yRot = 0;
            playerModel.jacket.zRot = 0;
        }
        ci.cancel();
    }
}
