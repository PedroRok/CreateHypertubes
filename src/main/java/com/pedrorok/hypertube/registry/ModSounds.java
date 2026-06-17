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
            () -> SoundEvent.createVariableRangeEvent(HypertubeMod.of("suction")));

    public static final DeferredHolder<SoundEvent, SoundEvent> TRAVELING = SOUNDS.register("traveling",
            () -> SoundEvent.createVariableRangeEvent(HypertubeMod.of("traveling")));

    public static final DeferredHolder<SoundEvent, SoundEvent> HYPERTUBE_ENTRANCE_OPEN = SOUNDS.register("entrance_open",
            () -> SoundEvent.createVariableRangeEvent(HypertubeMod.of("entrance_open")));

    public static final DeferredHolder<SoundEvent, SoundEvent> HYPERTUBE_ENTRANCE_CLOSE = SOUNDS.register("entrance_close",
            () -> SoundEvent.createVariableRangeEvent(HypertubeMod.of("entrance_close")));

    public static final DeferredHolder<SoundEvent, SoundEvent> CHOSE_DIRECTION = SOUNDS.register("chose_direction",
            () -> SoundEvent.createVariableRangeEvent(HypertubeMod.of("chose_direction")));

    public static void register(IEventBus eventBus) {
        SOUNDS.register(eventBus);
    }

}
