package com.pedrorok.hypertube.mixin.core;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * @author Rok, Pedro Lucas nmm. Created on 22/04/2025
 * @project Create Hypertube
 */
@Mixin(PlayerModel.class)
public interface PlayerModelAccessor {
    
    @Accessor("cloak")
    ModelPart createHypertube$getCloak();
    
}