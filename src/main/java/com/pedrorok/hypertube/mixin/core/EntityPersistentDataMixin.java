package com.pedrorok.hypertube.mixin.core;

import com.pedrorok.hypertube.mixin.core.EntityPersistentData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class EntityPersistentDataMixin implements EntityPersistentData {

    @Unique
    private CompoundTag persistentData;

    @Override
    public CompoundTag getPersistentData() {
        if (persistentData == null) {
            persistentData = new CompoundTag();
        }
        return persistentData;
    }

    /* =========================
       PERSISTÊNCIA EM DISCO
       ========================= */

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void writePersistentData(CompoundTag par1, CallbackInfo ci) {
        if (persistentData != null && !persistentData.isEmpty()) {
            par1.put("create_hypertube:persistent_data", persistentData);
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void readPersistentData(CompoundTag tag, CallbackInfo ci) {
        if (tag.contains("create_hypertube:persistent_data")) {
            persistentData = tag.getCompound("create_hypertube:persistent_data");
        }
    }
}
