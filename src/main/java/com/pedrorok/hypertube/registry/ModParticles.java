package com.pedrorok.hypertube.registry;

import com.pedrorok.hypertube.HypertubeMod;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;

/**
 * @author Rok, Pedro Lucas nmm. Created on 01/07/2025
 * @project Create Hypertube
 */
public class ModParticles {
    public static final SimpleParticleType SUCTION_PARTICLE = new SimpleParticleType(true);

    public static void register() {
        Registry.register(BuiltInRegistries.PARTICLE_TYPE, 
                new ResourceLocation(HypertubeMod.MOD_ID, "suction_particle"), 
                SUCTION_PARTICLE);
    }

}
