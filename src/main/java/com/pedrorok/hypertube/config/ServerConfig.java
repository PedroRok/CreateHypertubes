package com.pedrorok.hypertube.config;

import com.pedrorok.hypertube.core.travel.TravelConstants;
import com.pedrorok.hypertube.utils.TubeUtils;
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
    public final  ForgeConfigSpec.DoubleValue SPEED_MULTIPLIER;

    public final ForgeConfigSpec.DoubleValue STRESS_IMPACT_ENTRANCE;
    public final ForgeConfigSpec.DoubleValue STRESS_IMPACT_ACCELERATOR;

    private ServerConfig(ForgeConfigSpec.Builder builder) {
        builder.comment("Change these settings to customize the server-side behavior of the mod.")
                .push("Travel Settings");

        ALLOW_FISH_TO_TRAVEL = builder
                .comment("Allow fish to go through the tubes. (experimental)")
                .define("allowFishTravel", true);

        ALLOW_VILLAGER_TO_TRAVEL = builder
                .comment("Allow villagers to go through the tubes.")
                .define("allowVillagerTravel", true);

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

        TubeUtils.SPEED_MULTIPLIER = SPEED_MULTIPLIER.get().floatValue();
    }
}
