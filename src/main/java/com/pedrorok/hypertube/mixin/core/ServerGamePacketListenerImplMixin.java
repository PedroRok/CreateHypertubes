package com.pedrorok.hypertube.mixin.core;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.pedrorok.hypertube.core.travel.TravelConstants;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * @author Rok, Pedro Lucas nmm. Created on 14/07/2025
 * @project Create Hypertube
 */
@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin {

    /**
     * FIXME: Don't just disable this check to handle sub-level freezing
     */
    @WrapOperation(method = "handleMovePlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;isChangingDimension()Z"))
    private boolean redirectIsChangingDimension(ServerPlayer instance, Operation<Boolean> original) {
        if (instance.getPersistentData().getBoolean(TravelConstants.TRAVEL_TAG)) return true;
        return original.call(instance);
    }
}
