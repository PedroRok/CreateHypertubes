package com.pedrorok.hypertube.mixin.compat.sable;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.pedrorok.hypertube.core.compat.sable.EntityForceTracking;
import com.pedrorok.hypertube.HypertubeMod;
import dev.ryanhcode.sable.sublevel.SubLevel;
import lombok.Setter;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = Entity.class, priority = 1200)
public abstract class EntityMixin implements EntityForceTracking {
    @Unique
    private SubLevel createHypertube$forceTrackSubLevel = null;

    @Unique
    @Override
    public void createHypertube$setForceTrackSubLevel(SubLevel subLevel) {
        this.createHypertube$forceTrackSubLevel = subLevel;
    }

    /**
     * @return the sub-level the entity is standing on or locked to
     */
    @ModifyReturnValue(
        method = "sable$getTrackingSubLevel()Ldev/ryanhcode/sable/sublevel/SubLevel;", 
        remap = false,
        at = @At("RETURN")
    )
    public SubLevel createHypertube$getTrackingSubLevel(SubLevel original) {
        return this.createHypertube$forceTrackSubLevel != null ? this.createHypertube$forceTrackSubLevel : original;
    }
}