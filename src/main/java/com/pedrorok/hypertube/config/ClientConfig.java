package com.pedrorok.hypertube.config;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;

/**
 * @author Rok, Pedro Lucas nmm. Created on 05/06/2025
 * @project Create Hypertube
 */
public class ClientConfig {
    private static ClientConfig INSTANCE = new ClientConfig();

    public boolean ALLOW_FPV_INSIDE_TUBE = false;

    private ClientConfig() {
    }

    public static ClientConfig get() {
        return INSTANCE;
    }

    public static void init() {
        // Configuração será carregada do arquivo de configuração
        // Por enquanto, valores padrão são usados
    }
}
