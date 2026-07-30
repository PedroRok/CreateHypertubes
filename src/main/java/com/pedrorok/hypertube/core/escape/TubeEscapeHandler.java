package com.pedrorok.hypertube.core.escape;

import com.google.common.base.Strings;
import com.pedrorok.hypertube.core.travel.TravelConstants;
import com.pedrorok.hypertube.core.travel.client.ClientTravelPathMover;
import com.pedrorok.hypertube.network.packets.EscapeTubePacket;
import com.pedrorok.hypertube.registry.ModKeybinds;
import net.createmod.catnip.animation.LerpedFloat;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import com.pedrorok.hypertube.network.NetworkHandler;


/**
 * @author Rok, Pedro Lucas nmm. 29/06/2026
 * @project Create Hypertube
 */
public class TubeEscapeHandler {
    private static final int HOLD_DURATION_TICKS = 80;

    private static final LerpedFloat holdKeyProgress = LerpedFloat.linear().startWithValue(0);
    private static boolean packetSent = false;

    public static void onClientTick() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

        if (player == null || mc.level == null) {
            reset();
            return;
        }
        float value = holdKeyProgress.getValue();

        if (!player.getPersistentData().getBoolean(TravelConstants.TRAVEL_TAG)) {
            if (value < 0) return;
            holdKeyProgress.setValue(Math.max(0, value - .05f));
            return;
        }

        boolean shiftHeld = ModKeybinds.ESCAPE.isDown();

        if (!shiftHeld) {
            reset();
            if (value < 0) return;
            holdKeyProgress.setValue(Math.max(0, value - .05f));
            return;
        }

        if (packetSent) return;


        if (holdKeyProgress.getValue() >= 1f) {
            packetSent = true;
            sendPacketToServer();
        } else {
            holdKeyProgress.setValue(Math.min(1, value + Math.max(.10f, value) * .10f));
        }
    }

    public static void onRenderGuiOverlay(GuiGraphics guiGraphics, float partialTick) {

        float partialTicks = partialTick;
        float progress = holdKeyProgress.getValue(partialTicks);

        if (progress <= 0.1f) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        if (!mc.player.getPersistentData().getBoolean(TravelConstants.TRAVEL_TAG)) return;

        Component bar = makeProgressBar(Math.min(1f, ((progress * 8f / 7f) - 0.1f) * 1.1F));

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        Font font = mc.font;
        int textWidth = font.width(bar);
        int x = (screenWidth - textWidth) / 2;
        int y = screenHeight - 59;

        guiGraphics.drawString(font, bar, x, y, 0xFFFFFFFF, true);
    }

    private static Component makeProgressBar(float progress) {
        Font font = Minecraft.getInstance().font;

        float charWidth = font.width("|");

        int total = (int) (100 / charWidth);
        int current = (int) (progress * total);

        if (progress < 1f) {
            String bars = ChatFormatting.WHITE + Strings.repeat("|", current)
                    + ChatFormatting.DARK_GRAY + Strings.repeat("|", total - current);
            return Component.literal(bars);
        }

        return Component.literal(ChatFormatting.GREEN + Strings.repeat("|", total));
    }

    private static void reset() {
        packetSent = false;
    }

    private static void sendPacketToServer() {
        NetworkHandler.INSTANCE.sendToServer(new EscapeTubePacket());
        ClientTravelPathMover.stopMoving(Minecraft.getInstance().player.getId());
    }
}
