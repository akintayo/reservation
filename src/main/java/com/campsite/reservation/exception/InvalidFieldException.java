package com.campsite.reservation.exception;

public class InvalidFieldException extends ReservationException {

    public static final String INVALID_RESERVATION_DATE = "invalid-reservation-date";

    public InvalidFieldException(String message) {
        super(message, INVALID_RESERVATION_DATE);
    }
}
