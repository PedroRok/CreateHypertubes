package com.pedrorok.hypertube.registry;

import com.pedrorok.hypertube.HypertubeMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * @author Rok, Pedro Lucas nmm. Created on 26/05/2025
 * @project Create Hypertube
 */
public class ModSounds {

    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, HypertubeMod.MOD_ID);


    public static final DeferredHolder<SoundEvent, SoundEvent> HYPERTUBE_SUCTION = SOUNDS.register("suction",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(HypertubeMod.MOD_ID, "suction")));

    public static final DeferredHolder<SoundEvent, SoundEvent> TRAVELING = SOUNDS.register("traveling",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(HypertubeMod.MOD_ID, "traveling")));

    public static final DeferredHolder<SoundEvent, SoundEvent> HYPERTUBE_ENTRANCE_OPEN = SOUNDS.register("hypertube_entrance_open",
            () -> SoundEvent.createVariableRangeEvent(SoundEvents.IRON_TRAPDOOR_OPEN.getLocation()));

    public static final DeferredHolder<SoundEvent, SoundEvent> HYPERTUBE_ENTRANCE_CLOSE = SOUNDS.register("hypertube_entrance_close",
            () -> SoundEvent.createVariableRangeEvent(SoundEvents.IRON_TRAPDOOR_CLOSE.getLocation()));

    public static void register(IEventBus eventBus) {
        SOUNDS.register(eventBus);
    }

}
