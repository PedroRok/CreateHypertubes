package com.pedrorok.hypertube.registry;

import com.pedrorok.hypertube.HypertubeMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

/**
 * @author Rok, Pedro Lucas nmm. Created on 26/05/2025
 * @project Create Hypertube
 */
public class ModSounds {

    public static final SoundEvent HYPERTUBE_SUCTION = SoundEvent.createVariableRangeEvent(
            new ResourceLocation(HypertubeMod.MOD_ID, "suction"));

    public static final SoundEvent TRAVELING = SoundEvent.createVariableRangeEvent(
            new ResourceLocation(HypertubeMod.MOD_ID, "traveling"));

    public static final SoundEvent HYPERTUBE_ENTRANCE_OPEN = SoundEvent.createVariableRangeEvent(
            SoundEvents.IRON_TRAPDOOR_OPEN.getLocation());

    public static final SoundEvent HYPERTUBE_ENTRANCE_CLOSE = SoundEvent.createVariableRangeEvent(
            SoundEvents.IRON_TRAPDOOR_CLOSE.getLocation());

    public static void register() {
        Registry.register(BuiltInRegistries.SOUND_EVENT, 
                new ResourceLocation(HypertubeMod.MOD_ID, "suction"), 
                HYPERTUBE_SUCTION);
        Registry.register(BuiltInRegistries.SOUND_EVENT, 
                new ResourceLocation(HypertubeMod.MOD_ID, "traveling"), 
                TRAVELING);
        Registry.register(BuiltInRegistries.SOUND_EVENT, 
                new ResourceLocation(HypertubeMod.MOD_ID, "hypertube_entrance_open"), 
                HYPERTUBE_ENTRANCE_OPEN);
        Registry.register(BuiltInRegistries.SOUND_EVENT, 
                new ResourceLocation(HypertubeMod.MOD_ID, "hypertube_entrance_close"), 
                HYPERTUBE_ENTRANCE_CLOSE);
    }

}
