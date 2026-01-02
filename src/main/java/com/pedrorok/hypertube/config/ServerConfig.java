package com.pedrorok.hypertube.config;

import com.pedrorok.hypertube.core.travel.TravelConstants;
import com.pedrorok.hypertube.utils.TubeUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Rok, Pedro Lucas nmm. Created on 27/06/2025
 * @project Create Hypertube
 */
public class ServerConfig {
    private static final ServerConfig INSTANCE = new ServerConfig();

    public EntityListMode ENTITY_LIST_MODE = EntityListMode.BLACKLIST;
    public List<String> ENTITY_WHITELIST = List.of(
            "minecraft:player",
            "minecraft:villager",
            "minecraft:wandering_trader",
            "create:package"
    );
    public List<String> ENTITY_BLACKLIST = List.of(
            "minecraft:wither",
            "minecraft:ender_dragon"
    );

    public double SPEED_MULTIPLIER = 1.0;
    public double STRESS_IMPACT_ENTRANCE = 4.0;
    public double STRESS_IMPACT_ACCELERATOR = 4.0;

    private final Set<EntityType<?>> cachedWhitelist = new HashSet<>();
    private final Set<EntityType<?>> cachedBlacklist = new HashSet<>();

    private ServerConfig() {
    }

    public static ServerConfig get() {
        return INSTANCE;
    }

    public void init() {
        loadEntityList(ENTITY_WHITELIST, cachedWhitelist);
        loadEntityList(ENTITY_BLACKLIST, cachedBlacklist);

        TubeUtils.SPEED_MULTIPLIER = (float) SPEED_MULTIPLIER;
    }

    private void loadEntityList(List<String> entityIds, Set<EntityType<?>> targetSet) {
        targetSet.clear();
        for (String entityId : entityIds) {
            try {
                ResourceLocation location = ResourceLocation.tryParse(entityId);
                EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(location);

                if (entityType != null) {
                    targetSet.add(entityType);
                } else {
                    System.err.println("[Hypertube] Unknown entity type in config: " + entityId);
                }
            } catch (Exception e) {
                System.err.println("[Hypertube] Invalid entity ID in config: " + entityId + " - " + e.getMessage());
            }
        }
    }

    public static boolean canEntityTravel(EntityType<?> type) {
        boolean isInTag = type.is(TravelConstants.TRAVELLER_ENTITIES);
        return get().canEntityTravel(type, isInTag);
    }

    public boolean canEntityTravel(EntityType<?> entityType, boolean isInTag) {
        return switch (ENTITY_LIST_MODE) {
            case TAG_ONLY -> isInTag;
            case WHITELIST -> cachedWhitelist.contains(entityType);
            case BLACKLIST -> !cachedBlacklist.contains(entityType);
            case TAG_WITH_BLACKLIST -> isInTag && !cachedBlacklist.contains(entityType);
        };
    }

    public Set<EntityType<?>> getWhitelist() {
        return cachedWhitelist;
    }

    public Set<EntityType<?>> getBlacklist() {
        return cachedBlacklist;
    }

    public enum EntityListMode {
        TAG_ONLY,
        WHITELIST,
        BLACKLIST,
        TAG_WITH_BLACKLIST
    }
}
