package com.pedrorok.hypertube.core.travel.client;

import com.pedrorok.hypertube.network.packets.MoveDirectionPacket;
import com.pedrorok.hypertube.registry.ModSounds;
import com.pedrorok.hypertube.utils.MoveDirection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * @author Rok, Pedro Lucas nmm. 16/06/2026
 * @project Create Hypertube
 */
public class ClientKeyInputTracker {

    private static boolean wasForwardDown = false;
    private static boolean wasLeftDown = false;
    private static boolean wasRightDown = false;

    private static MoveDirection lastDirection = MoveDirection.RIGHT;

    public static void handlePlayerStart() {
        PacketDistributor.sendToServer(new MoveDirectionPacket(lastDirection));
    }

    public static MoveDirection handlePlayerInputs() {
        var options = Minecraft.getInstance().options;
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return MoveDirection.NONE;

        boolean forwardDown = options.keyUp.isDown();
        boolean leftDown = options.keyLeft.isDown();
        boolean rightDown = options.keyRight.isDown();

        if (forwardDown && !wasForwardDown) {
            lastDirection = MoveDirection.FRONT;
            player.playSound(ModSounds.CHOSE_DIRECTION.get(), 1.0f, 0.7f + player.level().random.nextFloat() * 0.2f);
        }
        if (leftDown && !wasLeftDown) {
            lastDirection = MoveDirection.LEFT;
            player.playSound(ModSounds.CHOSE_DIRECTION.get(), 1.0f, 0.7f + player.level().random.nextFloat() * 0.2f);
        }
        if (rightDown && !wasRightDown) {
            lastDirection = MoveDirection.RIGHT;
            player.playSound(ModSounds.CHOSE_DIRECTION.get(), 1.0f, 0.7f + player.level().random.nextFloat() * 0.2f);
        }

        wasForwardDown = forwardDown;
        wasLeftDown = leftDown;
        wasRightDown = rightDown;
        return lastDirection;
    }

    public static boolean hasPlayerPressedAnyKey() {
        var options = Minecraft.getInstance().options;
        return options.keyUp.isDown() || options.keyLeft.isDown() || options.keyRight.isDown();
    }
}
