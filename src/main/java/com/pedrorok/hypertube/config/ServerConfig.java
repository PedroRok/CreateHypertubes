package com.pedrorok.hypertube.config;

import com.pedrorok.hypertube.core.travel.TravelConstants;
import com.pedrorok.hypertube.utils.TubeUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Rok, Pedro Lucas nmm. Created on 27/06/2025
 * @project Create Hypertube
 */
public class ServerConfig {
    public static final ModConfigSpec SPEC;
    private static final ServerConfig INSTANCE;

    public final ModConfigSpec.EnumValue<EntityListMode> ENTITY_LIST_MODE;
    public final ModConfigSpec.ConfigValue<List<? extends String>> ENTITY_WHITELIST;
    public final ModConfigSpec.ConfigValue<List<? extends String>> ENTITY_BLACKLIST;

    public final ModConfigSpec.DoubleValue SPEED_MULTIPLIER;
    public final ModConfigSpec.DoubleValue STRESS_IMPACT_ENTRANCE;
    public final ModConfigSpec.DoubleValue STRESS_IMPACT_ACCELERATOR;

    private final Set<EntityType<?>> cachedWhitelist = new HashSet<>();
    private final Set<EntityType<?>> cachedBlacklist = new HashSet<>();

    private ServerConfig(ModConfigSpec.Builder builder) {
        builder.comment("Change these settings to customize the server-side behavior of the mod.")
                .push("Travel Settings");

        ENTITY_LIST_MODE = builder
                .comment("How to handle entity travel permissions:",
                        "TAG_ONLY - Use only the 'create_hypertube:traveller_entities' tag from datapacks",
                        "WHITELIST - Only entities in the whitelist can travel (ignores tag)",
                        "BLACKLIST - All entities can travel except those in the blacklist",
                        "TAG_WITH_BLACKLIST - Use tag but exclude entities in the blacklist")
                .defineEnum("entityListMode", EntityListMode.BLACKLIST);

        ENTITY_WHITELIST = builder
                .comment("Entities that CAN travel (only used when mode is WHITELIST).",
                        "Use entity registry names like 'minecraft:villager' or 'create:package'")
                .defineListAllowEmpty(
                        List.of("entityWhitelist"),
                        () -> List.of(
                                "minecraft:player",
                                "minecraft:villager",
                                "minecraft:wandering_trader",
                                "create:package"
                        ),
                        obj -> obj instanceof String
                );

        ENTITY_BLACKLIST = builder
                .comment("Entities that CANNOT travel (used in BLACKLIST and TAG_WITH_BLACKLIST modes).",
                        "Use entity registry names like 'minecraft:creeper' or 'minecraft:wither'")
                .defineListAllowEmpty(
                        List.of("entityBlacklist"),
                        () -> List.of(
                                "minecraft:wither",
                                "minecraft:ender_dragon"
                        ),
                        obj -> obj instanceof String
                );

        SPEED_MULTIPLIER = builder
                .comment("Multiplier for the speed of the tubes. Default is 1.0, which is normal speed. (THIS IS HIGHLY EXPERIMENTAL)")
                .defineInRange("speedMultiplier", 1.0, 0.5, 99.0);

        builder.pop();

        builder.comment("Stress Settings")
                .push("Stress Settings");

        STRESS_IMPACT_ENTRANCE = builder
                .comment("Stress impact of the Hyper Entrance block.")
                .defineInRange("entranceStressImpact", 4.0, 0.0, 100.0);

        STRESS_IMPACT_ACCELERATOR = builder
                .comment("Stress impact of the Hyper Accelerator block.")
                .defineInRange("acceleratorStressImpact", 4.0, 0.0, 100.0);

        builder.pop();
    }

    static {
        Pair<ServerConfig, ModConfigSpec> pair = new ModConfigSpec.Builder().configure(ServerConfig::new);
        INSTANCE = pair.getLeft();
        SPEC = pair.getRight();
    }

    public static ServerConfig get() {
        return INSTANCE;
    }

    public void init() {
        loadEntityList(ENTITY_WHITELIST.get(), cachedWhitelist);
        loadEntityList(ENTITY_BLACKLIST.get(), cachedBlacklist);

        TubeUtils.SPEED_MULTIPLIER = SPEED_MULTIPLIER.get().floatValue();
    }

    private void loadEntityList(List<? extends String> entityIds, Set<EntityType<?>> targetSet) {
        targetSet.clear();
        for (String entityId : entityIds) {
            try {
                ResourceLocation location = ResourceLocation.parse(entityId);
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
        return switch (ENTITY_LIST_MODE.get()) {
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
