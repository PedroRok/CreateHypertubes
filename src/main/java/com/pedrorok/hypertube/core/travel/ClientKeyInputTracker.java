package com.pedrorok.hypertube.core.travel;

import com.pedrorok.hypertube.network.packets.MoveDirectionPacket;
import com.pedrorok.hypertube.utils.MoveDirection;
import net.minecraft.client.Minecraft;
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
        if (Minecraft.getInstance().player == null) return MoveDirection.NONE;

        boolean forwardDown = options.keyUp.isDown();
        boolean leftDown = options.keyLeft.isDown();
        boolean rightDown = options.keyRight.isDown();

        if (forwardDown && !wasForwardDown) {
            lastDirection = MoveDirection.FRONT;
        }
        if (leftDown && !wasLeftDown) {
            lastDirection = MoveDirection.LEFT;
        }
        if (rightDown && !wasRightDown) {
            lastDirection = MoveDirection.RIGHT;
        }

        wasForwardDown = forwardDown;
        wasLeftDown = leftDown;
        wasRightDown = rightDown;
        return lastDirection;
    }
}
