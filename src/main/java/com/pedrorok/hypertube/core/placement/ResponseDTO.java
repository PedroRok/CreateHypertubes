package com.pedrorok.hypertube.core.placement;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

/**
 * @author Rok, Pedro Lucas nmm. Created on 27/05/2025
 * @project Create Hypertube
 */
public record ResponseDTO(boolean valid, String errorMessage) {

    public static ResponseDTO get(boolean valid) {
        return new ResponseDTO(valid, "");
    }
    public static ResponseDTO get(boolean valid, String errorMessage) {
        return new ResponseDTO(valid, errorMessage);
    }

    public static ResponseDTO invalid(String errorMessageKey) {
        return new ResponseDTO(false, errorMessageKey);
    }
    public static ResponseDTO invalid() {
        return new ResponseDTO(false, "");
    }

    public MutableComponent getMessageComponent() {
        if (errorMessage.isEmpty()) {
            return Component.empty();
        }
        MutableComponent translatable = Component.translatable(errorMessage);
        if (valid) return translatable;
        return translatable.withColor(0xFF0000);
    }
}
