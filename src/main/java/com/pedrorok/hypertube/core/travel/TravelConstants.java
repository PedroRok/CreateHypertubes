package com.pedrorok.hypertube.core.travel;

import net.minecraft.world.entity.EntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Rok, Pedro Lucas nmm. Created on 15/06/2025
 * @project Create Hypertube
 */
public class TravelConstants {

    public static final String TRAVEL_TAG = "hypertube_travel";
    public static final String LAST_TRAVEL_TIME = "last_travel_time";

    public static final String LAST_TRAVEL_BLOCKPOS = "last_travel_blockpos";
    public static final String LAST_TRAVEL_SPEED = "last_travel_speed";

    public static final String IMMUNITY_TAG = "hypertube_immunity";

    public static final int DEFAULT_TRAVEL_TIME = 2000;
    public static final int DEFAULT_AFTER_TUBE_CAMERA = 1500; // 0.5 seconds (subtracting default travel time)

    public static final float DEFAULT_SPEED_MULTIPLIER = 1;

    public static final Set<EntityType<?>> TRAVELLER_ENTITIES = new HashSet<>(Set.of(
            EntityType.PLAYER,
            EntityType.VILLAGER,
            EntityType.WANDERING_TRADER,
            EntityType.SALMON,
            EntityType.COD,
            EntityType.TROPICAL_FISH
    ));

    @OnlyIn(Dist.CLIENT)
    public static class Client {
        public static final Map<EntityType<?>, TravellerEntity> ENTITIES_RENDER = Map.of(
                EntityType.PLAYER, TravellerEntity.ofBiped(-0.5f),
                EntityType.VILLAGER, TravellerEntity.ofBiped(-0.8f),
                EntityType.WANDERING_TRADER, TravellerEntity.ofBiped(-0.8f),
                EntityType.SALMON, TravellerEntity.ofFish(0.9f),
                EntityType.COD, TravellerEntity.ofFish(1f),
                EntityType.TROPICAL_FISH, TravellerEntity.ofFish(1f)
        );
    }
}
