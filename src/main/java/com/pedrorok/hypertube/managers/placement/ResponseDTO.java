package com.pedrorok.hypertube.managers.placement;

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

    public static ResponseDTO invalid(String errorMessage) {
        return new ResponseDTO(false, errorMessage);
    }
}
