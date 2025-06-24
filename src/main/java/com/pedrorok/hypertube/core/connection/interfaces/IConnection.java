package com.pedrorok.hypertube.core.connection.interfaces;

import com.pedrorok.hypertube.core.connection.BezierConnection;
import com.pedrorok.hypertube.core.connection.SimpleConnection;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * @author Rok, Pedro Lucas nmm. Created on 17/06/2025
 * @project Create Hypertube
 */
public interface IConnection {

    @Nullable
    BezierConnection getThisEntranceConnection(Level level);

    Direction getThisEntranceDirection(Level level);

    boolean isSameConnection(IConnection connection);

    SimpleConnection getThisConnection();
}