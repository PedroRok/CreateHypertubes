package com.pedrorok.hypertube.core.data;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum JunctionMode implements StringRepresentable {
    FORCED_CENTER,
    FORCED_CONTINUE,
    AUTOMATIC;

    @Override
    public @NotNull String getSerializedName() {
        return switch (this) {
            case FORCED_CENTER -> "forced_center";
            case FORCED_CONTINUE -> "forced_continue";
            case AUTOMATIC -> "automatic";
        };
    }
}