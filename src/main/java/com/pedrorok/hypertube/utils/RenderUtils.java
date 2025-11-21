package com.pedrorok.hypertube.utils;

import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.core.Direction;

/**
 * @author Rok, Pedro Lucas nmm. Created on 19/11/2025
 * @project Create Hypertube
 */
public class RenderUtils {

    public static void rotateAroundCenterVertical(SuperByteBuffer buffer, int degreesRotated) {
        buffer.translate(0.5f, 0.5f, 0.5f);
        buffer.rotateY((float) Math.toRadians(degreesRotated));
        buffer.translate(-0.5f, -0.5f, -0.5f);
    }

    public static void rotateAroundCenterHorizontalZ(SuperByteBuffer buffer, int degreesRotated) {
        buffer.translate(0.5f, 0.5f, 0.5f);
        buffer.rotateZ((float) Math.toRadians(degreesRotated));
        buffer.translate(-0.5f, -0.5f, -0.5f);
    }

    public static void rotateToFace(SuperByteBuffer model, Direction tubeFacing, Direction attachmentDirection, boolean isTubeVertical) {

        switch (attachmentDirection) {
            case NORTH -> rotateAroundCenterVertical(model, -90);
            case EAST -> rotateAroundCenterVertical(model, 180);
            case SOUTH -> rotateAroundCenterVertical(model, 90);
            case WEST -> rotateAroundCenterVertical(model, 0);
            case UP -> rotateAroundCenterHorizontalZ(model, -90);
            case DOWN -> rotateAroundCenterHorizontalZ(model, 90);
        }

        if (!isTubeVertical) {
            if ((attachmentDirection == Direction.UP || attachmentDirection == Direction.DOWN) && tubeFacing != Direction.NORTH && tubeFacing != Direction.SOUTH) return;
            model.rotateXCenteredDegrees(90);

        }
    }
}