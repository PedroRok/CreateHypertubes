package com.pedrorok.hypertube.core.travel.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.pedrorok.hypertube.blocks.blockentities.HyperJunctionBlockEntity;
import com.pedrorok.hypertube.core.camera.DetachedCameraController;
import com.pedrorok.hypertube.core.connection.BezierConnection;
import com.pedrorok.hypertube.core.connection.interfaces.IConnection;
import com.pedrorok.hypertube.core.travel.TravelConstants;
import com.pedrorok.hypertube.network.packets.MoveDirectionPacket;
import com.pedrorok.hypertube.utils.JunctionDirectionUtils;
import com.pedrorok.hypertube.utils.MoveDirection;
import com.pedrorok.hypertube.utils.TubePulseRenderer;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.placement.PlacementClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

/**
 * @author Rok, Pedro Lucas nmm. 17/06/2026
 * @project Create Hypertube
 */
public class ClientTravelPathRender {

    private static MoveDirection lastValidMoveDir = MoveDirection.RIGHT;
    @Nullable
    private static Direction lastValidDirection = null;
    private static boolean isTraveling = false;

    protected static void handleStart(boolean endJunction, ClientTravelPathMover.PathData data) {
        lastValidMoveDir = MoveDirection.RIGHT;
        lastValidDirection = null;
        if (!endJunction) return;
        Tuple<Direction, MoveDirection> directionTuple = JunctionDirectionUtils.resolveValidDirectionTuple(MoveDirection.RIGHT, data.getLastBlockPos(), Minecraft.getInstance().player.level(), data.getJunctionDirection());
        if (directionTuple != null) {
            System.out.println("Resolved direction tuple: " + directionTuple.getA() + " " + directionTuple.getB());
            lastValidDirection = directionTuple.getA();
            lastValidMoveDir = directionTuple.getB();
        }
        PacketDistributor.sendToServer(new MoveDirectionPacket(lastValidMoveDir));
        isTraveling = true;
    }

    protected static void handleClientPlayer(ClientTravelPathMover.PathData data) {

        if (!data.isJunctionEnd()) return;
        MoveDirection direction = ClientKeyInputTracker.handlePlayerInputs();
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;


        Tuple<Direction, MoveDirection> directionTuple = JunctionDirectionUtils.resolveValidDirectionTuple(direction, data.getLastBlockPos(), player.level(), data.getJunctionDirection());
        if (directionTuple != null) {
            direction = directionTuple.getB();
        }
        lastValidMoveDir = direction;
        if (directionTuple != null) {
            lastValidDirection = directionTuple.getA();
        }
        if (lastValidDirection == null) return;

        if (ClientKeyInputTracker.hasPlayerPressedAnyKey()) {
            PacketDistributor.sendToServer(new MoveDirectionPacket(lastValidMoveDir));
        }

        if (player.tickCount % 10 != 0)
            return;
        if (!(player.level().getBlockEntity(data.getLastBlockPos()) instanceof HyperJunctionBlockEntity junctionBlock))
            return;

        Direction renderDir = lastValidDirection;
        IConnection connectionInDirection = junctionBlock.getConnectionInDirection(renderDir);

        if (connectionInDirection == null)
            return;
        BezierConnection connection = connectionInDirection.getThisEntranceConnection(junctionBlock.getLevel());
        if (connection == null)
            return;

        boolean inverted = connection.isInverted(data.getLastBlockPos());
        BlockPos pos = connection.getFromPos().pos();
        TubePulseRenderer.start(pos, connection, inverted, 8, 0.08f, 0.1f, 0x88FF88, 5);
    }

    public static void renderOverlay(GuiGraphics guiGraphics, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.gameMode == null) return;
        if (mc.options.hideGui || mc.gameMode.getPlayerMode() == GameType.SPECTATOR)
            return;

        if (!isTraveling) return;
        Entity cameraEntity = Minecraft.getInstance()
                .getCameraEntity();
        if (cameraEntity == null) {
            isTraveling = false;
            return;
        }
        LocalPlayer player = mc.player;
        if (player == null) return;
        if (!player.getPersistentData().getBoolean(TravelConstants.TRAVEL_TAG)) return;
        if (lastValidDirection == null) return;

        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        poseStack.translate((double) guiGraphics.guiWidth() / 2 - 91, guiGraphics.guiHeight() / 1.5, 0);

        // Direction

        float direction = lastValidDirection.toYRot();

        float snapSize = 22.5f;
        float diff = AngleHelper.getShortestAngleDiff(DetachedCameraController.get().getYaw(), direction);
        if (Math.abs(diff) < 60)
            diff = 0;

        float snappedAngle = (snapSize * Math.round(diff / snapSize)) % 360f;

        poseStack.translate(91, -9, 0);
        poseStack.scale(0.925f, 0.925f, 1);
        PlacementClient.textured(poseStack, 0, 0, 1, snappedAngle);

        poseStack.popPose();
    }
}
