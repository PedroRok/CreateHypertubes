package com.pedrorok.hypertube.core.connection;

import com.pedrorok.hypertube.core.connection.interfaces.IBezierConnection;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author Rok, Pedro Lucas nmm. Created on 22/07/2025
 * @project Create Hypertube
 */
public class BezierDTO extends IBezierConnection<ConnDTO> {

    public BezierDTO(ConnDTO fromPos, @Nullable ConnDTO toPos) {
        super(fromPos, toPos, toPos != null ? (int) Math.max(3, fromPos.pos().getCenter().distanceTo(toPos.pos().getCenter())) : 0);
    }

    @Override
    public Vec3 getFromPosCenter() {
        return null;
    }

    @Override
    public Vec3 getToPosCenter() {
        return null;
    }
}
