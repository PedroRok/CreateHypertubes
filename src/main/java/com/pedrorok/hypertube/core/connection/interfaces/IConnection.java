package com.pedrorok.hypertube.core.connection.interfaces;

import com.pedrorok.hypertube.core.connection.BezierConnection;
import com.pedrorok.hypertube.core.connection.SimpleConnection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * @author Rok, Pedro Lucas nmm. Created on 17/06/2025
 * @project Create Hypertube
 */
public interface IConnection {

    @Nullable
    public abstract BezierConnection getThisEntranceConnection(Level level);

    public abstract Direction getThisEntranceDirection(Level level);

    public abstract boolean isSameConnection(IConnection connection);

    public abstract SimpleConnection getThisConnection();


    public static SimpleConnection getSameConnectionBlockPos(IConnection conn, Level world, BlockPos pos) {
        if (conn != null) {
            BezierConnection bezier = conn.getThisEntranceConnection(world);
            if (bezier != null) {
                SimpleConnection dir;
                if (bezier.getFromPos().pos().equals(pos)) {
                    dir = bezier.getFromPos();
                } else {
                    SimpleConnection toPos = bezier.getToPos();
                    dir = new SimpleConnection(toPos.pos(), toPos.direction().getOpposite());
                }
                return dir;
            }
        }
        return null;
    }
}