package com.pedrorok.hypertube.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

/**
 * @author Rok, Pedro Lucas nmm. Created on 05/06/2025
 * @project Create Hypertube
 */
public class ClientConfig {
    public static final ModConfigSpec SPEC;
    private static final ClientConfig INSTANCE;

    public final ModConfigSpec.BooleanValue ALLOW_FPV_INSIDE_TUBE;

    private ClientConfig(ModConfigSpec.Builder builder) {
        builder.comment("Change these settings to customize the client-side behavior of the mod.")
                .push("Gameplay");

        ALLOW_FPV_INSIDE_TUBE = builder
                .comment("Allow first-person view inside the tube. Default is false for better experience.")
                .translation("hypertube.config.client.allowFPVInsideTube")
                .define("allowFPVInsideTheTube", false);

        builder.pop();
    }

    static {
        Pair<ClientConfig, ModConfigSpec> pair = new ModConfigSpec.Builder().configure(ClientConfig::new);
        INSTANCE = pair.getLeft();
        SPEC = pair.getRight();
    }

    public static ClientConfig get() {
        return INSTANCE;
    }
}