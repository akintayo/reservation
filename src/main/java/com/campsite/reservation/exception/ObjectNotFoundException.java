package com.campsite.reservation.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ObjectNotFoundException extends ReservationException {

    private static final String ERROR_CODE = "reservation-not-found";

    public ObjectNotFoundException (String message) {
        super(message, ERROR_CODE);
    }

}
