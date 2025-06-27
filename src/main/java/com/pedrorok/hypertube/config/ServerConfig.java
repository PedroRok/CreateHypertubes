package com.pedrorok.hypertube.config;

import com.pedrorok.hypertube.core.travel.TravelConstants;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

/**
 * @author Rok, Pedro Lucas nmm. Created on 27/06/2025
 * @project Create Hypertube
 */
public class ServerConfig {
    public static final ForgeConfigSpec SPEC;
    private static final ServerConfig INSTANCE;

    public final ForgeConfigSpec.BooleanValue ALLOW_FISH_TO_TRAVEL;
    public final ForgeConfigSpec.BooleanValue ALLOW_VILLAGER_TO_TRAVEL;


    private ServerConfig(ForgeConfigSpec.Builder builder) {
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
        Pair<ServerConfig, ForgeConfigSpec> pair = new ForgeConfigSpec.Builder().configure(ServerConfig::new);
        INSTANCE = pair.getLeft();
        SPEC = pair.getRight();
    }

    public static ServerConfig get() {
        return INSTANCE;
    }

    public void init() {
        if (!ALLOW_FISH_TO_TRAVEL.get()) {
            TravelConstants.TRAVELLER_ENTITIES.remove(EntityType.SALMON);
            TravelConstants.TRAVELLER_ENTITIES.remove(EntityType.COD);
            TravelConstants.TRAVELLER_ENTITIES.remove(EntityType.TROPICAL_FISH);
        }

        if (!ALLOW_VILLAGER_TO_TRAVEL.get()) {
            TravelConstants.TRAVELLER_ENTITIES.remove(EntityType.VILLAGER);
            TravelConstants.TRAVELLER_ENTITIES.remove(EntityType.WANDERING_TRADER);
        }
    }
}
