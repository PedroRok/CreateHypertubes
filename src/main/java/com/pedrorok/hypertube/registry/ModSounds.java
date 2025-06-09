package com.pedrorok.hypertube.registry;

import com.pedrorok.hypertube.HypertubeMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/**
 * @author Rok, Pedro Lucas nmm. Created on 26/05/2025
 * @project Create Hypertube
 */
public class ModSounds {

    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, HypertubeMod.MOD_ID);


    public static final RegistryObject<SoundEvent> HYPERTUBE_SUCTION = SOUNDS.register("suction",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(HypertubeMod.MOD_ID, "suction")));

    public static final RegistryObject<SoundEvent> TRAVELING = SOUNDS.register("traveling",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(HypertubeMod.MOD_ID, "traveling")));

    public static void register(IEventBus eventBus) {
        SOUNDS.register(eventBus);
    }

}
