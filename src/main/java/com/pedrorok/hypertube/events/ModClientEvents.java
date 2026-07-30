package com.pedrorok.hypertube.events;

import com.pedrorok.hypertube.HypertubeMod;
import com.pedrorok.hypertube.client.particles.SuctionParticle;
import com.pedrorok.hypertube.ponder.HypertubesPonderScenes;
import com.pedrorok.hypertube.ponder.HypertubesPonderTags;
import com.pedrorok.hypertube.registry.ModBlocks;
import com.pedrorok.hypertube.registry.ModParticles;
import com.simibubi.create.infrastructure.ponder.PonderIndex;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * @author Rok, Pedro Lucas nmm. Created on 21/04/2025
 * @project Create Hypertube
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD, modid = HypertubeMod.MOD_ID)
public class ModClientEvents {

    @SubscribeEvent
    public static void forgeLoad(FMLClientSetupEvent event) {
        event.enqueueWork(ModClientEvents::setupRenderTypes);
        HypertubesPonderScenes.register();
        HypertubesPonderTags.register();
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
