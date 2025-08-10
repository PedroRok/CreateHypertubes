package com.pedrorok.hypertube.core.connection.interfaces;

import com.pedrorok.hypertube.core.travel.TravelPathMover;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;

/**
 * @author Rok, Pedro Lucas nmm. Created on 09/08/2025
 * @project Create Hypertube
 */
public interface ITubeActionPoint {

    public void handleTravelPath(LivingEntity entity, TravelPathMover mover, BlockPos pos);
}
