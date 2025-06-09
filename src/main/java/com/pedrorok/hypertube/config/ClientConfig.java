package com.pedrorok.hypertube.config;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

/**
 * @author Rok, Pedro Lucas nmm. Created on 05/06/2025
 * @project Create Hypertube
 */
public class ClientConfig {
    public static final ForgeConfigSpec SPEC;
    private static final ClientConfig INSTANCE;

    public final ForgeConfigSpec.BooleanValue ALLOW_FPV_INSIDE_TUBE;

    private ClientConfig(ForgeConfigSpec.Builder builder) {
        builder.comment("Change these settings to customize the client-side behavior of the mod.")
                .push("Gameplay");

        ALLOW_FPV_INSIDE_TUBE = builder
                .comment("Allow first-person view inside the tube. Default is false for better experience.")
                .translation("hypertube.config.client.allowFPVInsideTube")
                .define("allowFPVInsideTheTube", false);

        builder.pop();
    }

    static {
        Pair<ClientConfig, ForgeConfigSpec> pair = new ForgeConfigSpec.Builder().configure(ClientConfig::new);
        INSTANCE = pair.getLeft();
        SPEC = pair.getRight();
    }

    public static ClientConfig get() {
        return INSTANCE;
    }
}