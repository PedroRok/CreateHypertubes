package com.pedrorok.hypertube.registry;

import com.pedrorok.hypertube.HypertubeMod;
import com.simibubi.create.foundation.data.CreateRegistrate;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * @author Rok, Pedro Lucas nmm. Created on 26/05/2025
 * @project Create Hypertube
 */
public class ModSounds {

    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, HypertubeMod.MOD_ID);


    public static final DeferredHolder<SoundEvent, SoundEvent> HYPERTUBE_SUCTION = SOUNDS.register("suction",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(HypertubeMod.MOD_ID, "suction")));

    public static final DeferredHolder<SoundEvent, SoundEvent> WIND_TUNNEL = SOUNDS.register("wind_tunnel",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(HypertubeMod.MOD_ID, "wind_tunnel")));

    public static void register(IEventBus eventBus) {
        SOUNDS.register(eventBus);
    }

}
