package com.pedrorok.hypertube.events;

import com.pedrorok.hypertube.HypertubeMod;
import com.pedrorok.hypertube.registry.ModBlocks;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
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
    }

    private static void setupRenderTypes() {
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.HYPERTUBE.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.HYPERTUBE_ENTRANCE.get(), RenderType.translucent());
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // event.registerBlockEntityRenderer(ModBlockEntities.HYPERTUBE_ENTRANCE.get(), EntranceBlockEntityRenderer::new);
    }
}
