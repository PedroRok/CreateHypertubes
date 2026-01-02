package com.pedrorok.hypertube;

import com.pedrorok.hypertube.config.ClientConfig;
import com.pedrorok.hypertube.events.ModClientEvents;
import com.pedrorok.hypertube.events.ModServerEvents;
import com.pedrorok.hypertube.events.PlayerSyncEvents;
import com.pedrorok.hypertube.network.NetworkHandler;
import com.pedrorok.hypertube.config.ServerConfig;
import com.pedrorok.hypertube.core.smarttube.ITubeAttachment;
import com.pedrorok.hypertube.registry.*;
import com.simibubi.create.foundation.data.CreateRegistrate;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Rok, Pedro Lucas nmm. Created on 17/04/2025
 * @project Create Hypertube
 */
public class HypertubeMod implements ModInitializer {
    public static final String MOD_ID = "create_hypertube";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(HypertubeMod.MOD_ID);
    //.defaultCreativeTab((ResourceKey<CreativeModeTab>) null);

    @Override
    public void onInitialize() {
        NetworkHandler.init();
        ModPartialModels.init();

        ModBlocks.register();
        ModBlockEntities.register();
        ModItems.register();

        ModCreativeTab.register();

        ModParticles.register();

        ModSounds.register();

        ITubeAttachment.init();

        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            ServerConfig.get().init();
        });

        ModServerEvents.init();
        PlayerSyncEvents.init();
        
        // Registrar eventos do cliente
        if (net.fabricmc.loader.api.FabricLoader.getInstance().getEnvironmentType() == net.fabricmc.api.EnvType.CLIENT) {
            new ModClientEvents().onInitializeClient();
        }
    }

    public static CreateRegistrate get() {
        return REGISTRATE;
    }
}
