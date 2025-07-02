package com.pedrorok.hypertube.registry;

import com.pedrorok.hypertube.HypertubeMod;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * @author Rok, Pedro Lucas nmm. Created on 01/07/2025
 * @project Create Hypertube
 */
public class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLES =
            DeferredRegister.create(Registries.PARTICLE_TYPE, HypertubeMod.MOD_ID);

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SUCTION_PARTICLE =
            PARTICLES.register("suction_particle", () ->
                    new SimpleParticleType(true));

    public static void register(IEventBus modEventBus) {
        PARTICLES.register(modEventBus);
    }

}
