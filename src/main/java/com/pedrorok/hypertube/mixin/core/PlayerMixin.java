package com.pedrorok.hypertube.mixin.core;

import com.pedrorok.hypertube.core.camera.DetachedPlayerDirController;
import com.pedrorok.hypertube.core.travel.TravelConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Rok, Pedro Lucas nmm. Created on 19/06/2025
 * @project Create Hypertube
 */
@Mixin(Player.class)
public class PlayerMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        Player entity = (Player) (Object) this;

        if (!entity.getPersistentData().getBoolean(TravelConstants.TRAVEL_TAG)) return;
        if (!entity.level().isClientSide) return;

        createHypertube$tickInClient(entity);
    }

    @Unique
    @OnlyIn(Dist.CLIENT)
    private void createHypertube$tickInClient(Player player) {
        if (!Minecraft.getInstance().player.getUUID().equals(player.getUUID())) {
            return;
        }
        player.setYRot(DetachedPlayerDirController.get().getYaw());
        player.setXRot(DetachedPlayerDirController.get().getPitch());
    }
}
