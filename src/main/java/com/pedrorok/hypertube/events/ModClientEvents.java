package com.pedrorok.hypertube.events;

import com.pedrorok.hypertube.HypertubeMod;
import com.pedrorok.hypertube.client.particles.SuctionParticle;
import com.pedrorok.hypertube.ponder.HypertubesPonderPlugin;
import com.pedrorok.hypertube.registry.ModBlocks;
import com.pedrorok.hypertube.registry.ModParticles;
import net.createmod.ponder.foundation.PonderIndex;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

/**
 * @author Rok, Pedro Lucas nmm. Created on 21/04/2025
 * @project Create Hypertube
 */
@EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD, modid = HypertubeMod.MOD_ID)
public class ModClientEvents {

    @SubscribeEvent
    public static void forgeLoad(FMLClientSetupEvent event) {
        event.enqueueWork(ModClientEvents::setupRenderTypes);

        PonderIndex.addPlugin(new HypertubesPonderPlugin());
    }

    private static void setupRenderTypes() {
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.HYPERTUBE.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.HYPERTUBE_ENTRANCE.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.HYPER_ACCELERATOR.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.HYPER_JUNCTION.get(), RenderType.translucent());
    }

    @SubscribeEvent
    public static void registerParticles(RegisterParticleProvidersEvent event) {
        Minecraft.getInstance().particleEngine.register(
                ModParticles.SUCTION_PARTICLE.get(),
                SuctionParticle.Provider::new
        );
    }
}
