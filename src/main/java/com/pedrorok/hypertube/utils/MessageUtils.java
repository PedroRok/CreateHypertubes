package com.pedrorok.hypertube.utils;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

/**
 * @author Rok, Pedro Lucas nmm. Created on 27/05/2025
 * @project Create Hypertube
 */
public class MessageUtils {

    public static void sendActionMessage(Player player, Component message) {
        sendActionMessage(player, message, false);
    }

    public static void sendActionMessage(Player player, Component message, boolean forceStay) {
        if (!forceStay && player.getPersistentData().getLong("last_action_message_stay") > System.currentTimeMillis()) {
            return; // Don't send if the last message is still active
        }
        if (forceStay) {
            player.getPersistentData().putLong("last_action_message_stay", System.currentTimeMillis() + 2000);
        }
        player.displayClientMessage(message, true);
    }

    public static void sendActionMessage(Player player, String message) {
        sendActionMessage(player, Component.translatable(message));
    }
}
