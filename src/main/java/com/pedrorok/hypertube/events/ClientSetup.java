package com.pedrorok.hypertube.events;

import com.pedrorok.hypertube.HypertubeMod;
import com.pedrorok.hypertube.client.renderer.AcceleratorEntityRenderer;
import com.pedrorok.hypertube.client.renderer.EntranceBlockEntityRenderer;
import com.pedrorok.hypertube.client.renderer.HypertubeBlockEntityRenderer;
import com.pedrorok.hypertube.registry.ModBlockEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/**
 * @author Rok, Pedro Lucas nmm. Created on 21/04/2025
 * @project Create Hypertube
 */
@EventBusSubscriber(modid = HypertubeMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.HYPERTUBE_ENTRANCE.get(), EntranceBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.HYPERTUBE.get(), HypertubeBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.HYPER_ACCELERATOR.get(), AcceleratorEntityRenderer::new);
    }
}