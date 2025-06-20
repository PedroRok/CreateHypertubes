package com.pedrorok.hypertube.managers.travel;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * @author Rok, Pedro Lucas nmm. Created on 20/06/2025
 * @project Create Hypertube
 */
public record TravellerEntity(BiConsumer<LivingEntity, PoseStack> renderEntityOnTube) {

    public static TravellerEntity ofBiped() {
        BiConsumer<LivingEntity, PoseStack> renderBiped = (entity, poseStack) -> {
            poseStack.pushPose();
            poseStack.translate(0, 0.2, 0);
            poseStack.mulPose(Axis.YP.rotationDegrees(-entity.getYRot()));
            poseStack.mulPose(Axis.XP.rotationDegrees(entity.getXRot() + 90));
            poseStack.mulPose(Axis.YP.rotationDegrees(entity.getYRot()));
            poseStack.translate(0, -0.5, 0);
            poseStack.scale(0.8f, 0.8f, 0.8f);
        };
        return new TravellerEntity(renderBiped);
    }

    public static TravellerEntity ofFish(float size) {
        BiConsumer<LivingEntity, PoseStack> renderAnimal = (entity, poseStack) -> {
            entity.setPose(Pose.SWIMMING);
            poseStack.pushPose();
            poseStack.translate(0, 0.1, 0);
            poseStack.mulPose(Axis.YP.rotationDegrees(-entity.getYRot() + 90));
            poseStack.mulPose(Axis.ZP.rotationDegrees(entity.getXRot()));
            poseStack.mulPose(Axis.XP.rotationDegrees(-90));
            poseStack.mulPose(Axis.YP.rotationDegrees(entity.getYRot() - 90));
            poseStack.translate(0, -0.1,0);
            poseStack.scale(size, size, size);
        };
        return new TravellerEntity(renderAnimal);
    }
}
