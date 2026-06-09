package com.pedrorok.hypertube.core.connection.interfaces;

import com.pedrorok.hypertube.core.travel.ItemTravelManager;
import com.pedrorok.hypertube.core.travel.TravelPathMover;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public interface ITubeActionPoint {

    public void handleTravelPath(LivingEntity entity, @Nullable TravelPathMover mover, BlockPos pos);

    default void handleItemTravel(ItemTravelManager.TravelingItem item, Level level, BlockPos pos) {
    }
}
