package com.pedrorok.hypertube.core.travel;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.pedrorok.hypertube.core.travel.client.ClientTravelPathMover;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;

import java.util.function.BiConsumer;

/**
 * @author Rok, Pedro Lucas nmm. Created on 20/06/2025
 * @project Create Hypertube
 */
public record TravellerEntity(BiConsumer<LivingEntity, PoseStack> renderEntityOnTube) {

    public static TravellerEntity ofBiped(float translateY) {
        BiConsumer<LivingEntity, PoseStack> renderBiped = (entity, poseStack) -> {
            float yaw = entity.getYRot();
            float pitch = entity.getXRot();
            ClientTravelPathMover.PathData data = ClientTravelPathMover.getData(entity.getId());
            if (data != null) {
                pitch = data.getPitch();
            }

            poseStack.pushPose();
            poseStack.translate(0, 0.2, 0);
            poseStack.mulPose(Axis.YP.rotationDegrees(-yaw));
            poseStack.mulPose(Axis.XP.rotationDegrees(pitch + 90));
            poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
            poseStack.translate(0, translateY, 0);
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
            poseStack.translate(0, -0.1, 0);
            poseStack.scale(size, size, size);
        };
        return new TravellerEntity(renderAnimal);
    }

    public static TravellerEntity ofAny(float maxSize) {
        BiConsumer<LivingEntity, PoseStack> renderAny = (entity, poseStack) -> {
            float size = Math.min(maxSize, entity.getBbWidth());
            poseStack.pushPose();
            poseStack.translate(0, 0.1, 0);
            poseStack.mulPose(Axis.YP.rotationDegrees(-entity.getYRot()));
            poseStack.mulPose(Axis.XP.rotationDegrees(entity.getXRot()));
            poseStack.mulPose(Axis.YP.rotationDegrees(entity.getYRot()));
            poseStack.translate(0, -0.1, 0);
            poseStack.scale(size, size, size);
        };
        return new TravellerEntity(renderAny);

    }
}
