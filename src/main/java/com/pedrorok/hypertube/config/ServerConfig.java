package com.pedrorok.hypertube.config;

import com.pedrorok.hypertube.core.travel.TravelConstants;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

/**
 * @author Rok, Pedro Lucas nmm. Created on 27/06/2025
 * @project Create Hypertube
 */
public class ServerConfig {
    public static final ModConfigSpec SPEC;
    private static final ServerConfig INSTANCE;

    public final ModConfigSpec.BooleanValue ALLOW_FISH_TO_TRAVEL;
    public final ModConfigSpec.BooleanValue ALLOW_VILLAGER_TO_TRAVEL;


    private ServerConfig(ModConfigSpec.Builder builder) {
        builder.comment("Change these settings to customize the server-side behavior of the mod.")
                .push("Travel Settings");

        ALLOW_FISH_TO_TRAVEL = builder
                .comment("Allow fish to go through the tubes. (experimental)")
                .define("allowFishTravel", true);

        ALLOW_VILLAGER_TO_TRAVEL = builder
                .comment("Allow villagers to go through the tubes.")
                .define("allowVillagerTravel", true);

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
        if (!ALLOW_FISH_TO_TRAVEL.get()) {
            TravelConstants.ENTITIES_CAN_TRAVEL.remove(EntityType.SALMON);
            TravelConstants.ENTITIES_CAN_TRAVEL.remove(EntityType.COD);
            TravelConstants.ENTITIES_CAN_TRAVEL.remove(EntityType.TROPICAL_FISH);
        }

        if (!ALLOW_VILLAGER_TO_TRAVEL.get()) {
            TravelConstants.ENTITIES_CAN_TRAVEL.remove(EntityType.VILLAGER);
            TravelConstants.ENTITIES_CAN_TRAVEL.remove(EntityType.WANDERING_TRADER);
        }
    }
}
