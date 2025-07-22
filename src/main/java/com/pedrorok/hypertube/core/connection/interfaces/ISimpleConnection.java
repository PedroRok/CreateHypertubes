package com.pedrorok.hypertube.core.connection.interfaces;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

/**
 * @author Rok, Pedro Lucas nmm. Created on 22/07/2025
 * @project Create Hypertube
 */
public interface ISimpleConnection<T> {
    T pos();
    Direction direction();
}
