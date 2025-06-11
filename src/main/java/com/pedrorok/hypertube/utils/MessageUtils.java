package com.pedrorok.hypertube.utils;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

/**
 * @author Rok, Pedro Lucas nmm. Created on 27/05/2025
 * @project Create Hypertube
 */
public class MessageUtils {

    public static void sendActionMessage(Player player, Component message) {
        player.displayClientMessage(message, true);
    }

    public static void sendActionMessage(Player player, String message) {
        player.displayClientMessage(Component.literal(message), true);
    }
}
