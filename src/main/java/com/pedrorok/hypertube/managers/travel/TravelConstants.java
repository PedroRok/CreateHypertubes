package com.pedrorok.hypertube.managers.travel;

import net.minecraft.world.entity.EntityType;

import java.util.Map;
import java.util.Set;

/**
 *
 * @project Create Hypertube
 * @author Rok, Pedro Lucas nmm. Created on 15/06/2025
 */
public class TravelConstants {

    public static final String TRAVEL_TAG = "hypertube_travel";
    public static final String LAST_TRAVEL_TIME = "last_travel_time";

    public static final String LAST_TRAVEL_BLOCKPOS = "last_travel_blockpos";
    public static final String LAST_TRAVEL_SPEED = "last_travel_speed";
    public static final String LAST_POSITION = "last_travel_position";

    public static final String IMMUNITY_TAG = "hypertube_immunity";

    public static final int DEFAULT_TRAVEL_TIME = 2000;
    public static final int DEFAULT_AFTER_TUBE_CAMERA = 1500; // 0.5 seconds (subtracting default travel time)

    public static final double DEFAULT_MIN_SPEED = 0.5;
    public static final float DISTANCE_FROM_LINE_TP = 1.5f;

    public static final int LATENCY_THRESHOLD = 100; // ms

    public static final Map<EntityType<?>, TravellerEntity> ENTITIES_CAN_TRAVEL = Map.of(
            EntityType.PLAYER, TravellerEntity.ofBiped(),
            EntityType.VILLAGER, TravellerEntity.ofBiped(),
            EntityType.WANDERING_TRADER, TravellerEntity.ofBiped(),
            EntityType.SALMON, TravellerEntity.ofFish(0.9f),
            EntityType.COD, TravellerEntity.ofFish(1f),
            EntityType.TROPICAL_FISH, TravellerEntity.ofFish(1f)
    );
}
