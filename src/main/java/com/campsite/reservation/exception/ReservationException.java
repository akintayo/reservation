package com.campsite.reservation.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReservationException extends RuntimeException {

    private String errorCode;

    public ReservationException(String message, String errorCode) {
        super(message);
        this.setErrorCode(errorCode);
    }

    public ReservationException() {
        super();
    }

}
