package com.pedrorok.hypertube.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * @author Rok, Pedro Lucas nmm. Created on 27/05/2025
 * @project Create Hypertube
 */
public class MessageUtils {

    public static void sendActionMessage(Player player, String message) {
        player.displayClientMessage(Component.literal(message), true);
    }

    @OnlyIn(Dist.CLIENT)
    public static void sendClientActionMessage(String message) {
        sendActionMessage(Minecraft.getInstance().player, message);
    }
}
