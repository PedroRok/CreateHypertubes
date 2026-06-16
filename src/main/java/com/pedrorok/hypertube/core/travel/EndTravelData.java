package com.pedrorok.hypertube.core.travel;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

/**
 * @author Rok, Pedro Lucas nmm. 16/06/2026
 * @project Create Hypertube
 */
public record EndTravelData(LivingEntity entity, boolean isJunctionEnd,@Nullable Direction direction, boolean isForced) {

    public static EndTravelData forced(LivingEntity entity) {
        return new EndTravelData(entity, false, null, true);
    }

    public static EndTravelData normal(LivingEntity entity) {
        return new EndTravelData(entity, false, null, false);
    }

    public static EndTravelData forced(LivingEntity entity, boolean isJunctionEnd, @Nullable Direction direction) {
        return new EndTravelData(entity, isJunctionEnd, direction, true);
    }

    public static EndTravelData normal(LivingEntity entity, boolean isJunctionEnd,@Nullable  Direction direction) {
        return new EndTravelData(entity, isJunctionEnd, direction, false);
    }
}
