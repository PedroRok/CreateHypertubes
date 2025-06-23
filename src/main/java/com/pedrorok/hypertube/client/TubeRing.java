package com.pedrorok.hypertube.client;

import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.List;

/**
 * @author Rok, Pedro Lucas nmm. Created on 23/06/2025
 * @project Create Hypertube
 */
public record TubeRing(
        Vec3 center, List<Vector3f> exteriorOffsets, List<Vector3f> interiorOffsets,
        List<Vector3f> lineOffsets, float uCoordinate
) {
}