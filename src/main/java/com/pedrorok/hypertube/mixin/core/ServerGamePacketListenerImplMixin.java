package com.pedrorok.hypertube.mixin.core;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.pedrorok.hypertube.core.travel.TravelConstants;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.injection.At;

/**
 * @author Rok, Pedro Lucas nmm. Created on 14/07/2025
 * @project Create Hypertube
 */
@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin {

    @WrapOperation(
            method = "handleMovePlayer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerPlayer;isChangingDimension()Z"
            )
    )
    private boolean redirectIsChangingDimension(ServerPlayer player, Operation<Boolean> original) {
        if (player.getPersistentData().getBoolean(TravelConstants.TRAVEL_TAG)) return true;
        return original.call(player);
    }
}
