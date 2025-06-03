package com.pedrorok.hypertube.events;

import com.pedrorok.hypertube.HypertubeMod;
import com.pedrorok.hypertube.client.renderer.EntranceBlockEntityRenderer;
import com.pedrorok.hypertube.registry.ModBlockEntities;
import com.pedrorok.hypertube.registry.ModBlocks;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/**
 * @author Rok, Pedro Lucas nmm. Created on 21/04/2025
 * @project Create Hypertube
 */
@EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD, modid = HypertubeMod.MOD_ID)
public class ModClientEvents {

    @SubscribeEvent
    public static void forgeLoad(FMLClientSetupEvent event) {
        event.enqueueWork(ModClientEvents::setupRenderTypes);
    }

    private static void setupRenderTypes() {
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.HYPERTUBE.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.HYPERTUBE_ENTRANCE.get(), RenderType.translucent());
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.HYPERTUBE_ENTRANCE.get(), EntranceBlockEntityRenderer::new);
    }
}
