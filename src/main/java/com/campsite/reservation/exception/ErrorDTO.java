package com.campsite.reservation.exception;

import lombok.Getter;

@Getter
public class ErrorDTO {

    private final String message;
    private final String code;

    public ErrorDTO(String message, String code) {
        this.message = message;
        this.code = code;
    }
}
