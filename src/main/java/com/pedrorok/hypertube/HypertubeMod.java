package com.pedrorok.hypertube;

import com.pedrorok.hypertube.config.ClientConfig;
import com.pedrorok.hypertube.network.NetworkHandler;
import com.pedrorok.hypertube.config.ServerConfig;
import com.pedrorok.hypertube.registry.*;
import com.simibubi.create.foundation.data.CreateRegistrate;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Rok, Pedro Lucas nmm. Created on 17/04/2025
 * @project Create Hypertube
 */
@Mod(HypertubeMod.MOD_ID)
public class HypertubeMod {
    public static final String MOD_ID = "create_hypertube";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(HypertubeMod.MOD_ID);
    //.defaultCreativeTab((ResourceKey<CreativeModeTab>) null);

    public HypertubeMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::commonSetup);

        ModLoadingContext.get()
                .registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC, MOD_ID + "-client.toml");
        ModLoadingContext.get()
                .registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC, MOD_ID + "-server.toml");

        REGISTRATE.registerEventListeners(modEventBus);

        NetworkHandler.init();
        ModPartialModels.init();

        ModBlocks.register();
        ModBlockEntities.register();

        ModCreativeTab.register(modEventBus);

        ModSounds.register(modEventBus);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {

    }

    public static CreateRegistrate get() {
        return REGISTRATE;
    }
}
