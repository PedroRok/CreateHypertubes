package com.pedrorok.hypertube.mixin;

import com.pedrorok.hypertube.managers.TravelManager;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Rok, Pedro Lucas nmm. Created on 23/05/2025
 * @project Create Hypertube
 */
@Mixin(PlayerModel.class)
public abstract class PlayerModelMixin {

    @Inject(method = "setupAnim*", at = @At("RETURN"))
    private void onSetupAnim(LivingEntity entity, float limbSwing, float limbSwingAmount,
                             float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        if (!(entity instanceof Player player)
            || !player.getPersistentData().getBoolean(TravelManager.TRAVEL_TAG)) return;


        PlayerModel<?> model = (PlayerModel<?>) (Object) this;

        model.rightArm.xRot = 0;
        model.rightArm.yRot = 0;
        model.rightArm.zRot = 0;

        model.rightSleeve.xRot = 0;
        model.rightSleeve.yRot = 0;
        model.rightSleeve.zRot = 0;


        model.leftArm.xRot = 0;
        model.leftArm.yRot = 0;
        model.leftArm.zRot = 0;

        model.leftSleeve.xRot = 0;
        model.leftSleeve.yRot = 0;
        model.leftSleeve.zRot = 0;


        model.rightLeg.xRot = 0;
        model.rightLeg.yRot = 0;
        model.rightLeg.zRot = 0;

        model.rightPants.xRot = 0;
        model.rightPants.yRot = 0;
        model.rightPants.zRot = 0;

        model.leftLeg.xRot = 0;
        model.leftLeg.yRot = 0;
        model.leftLeg.zRot = 0;

        model.leftPants.xRot = 0;
        model.leftPants.yRot = 0;
        model.leftPants.zRot = 0;

        model.body.xRot = 0;
        model.body.yRot = 0;
        model.body.zRot = 0;

        model.head.xRot = -1.2F;
        model.head.yRot = 0;
        model.head.zRot = 0;

        model.hat.xRot = -1.2F;
        model.hat.yRot = 0;
        model.hat.zRot = 0;

        model.jacket.xRot = 0;
        model.jacket.yRot = 0;
        model.jacket.zRot = 0;
    }
}
