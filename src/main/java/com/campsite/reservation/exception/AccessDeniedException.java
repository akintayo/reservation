package com.campsite.reservation.exception;

public class AccessDeniedException extends ReservationException {

    private static final String USER_NOT_PERMITTED = "not-permitted";

    private static final String CONFLICT_RESERVATION = "reservation-conflict";

    private AccessDeniedException(String message, String errorCode) {
        super(message, errorCode);
    }

    public static AccessDeniedException reservationConflict (String message) {
        return new AccessDeniedException(message, CONFLICT_RESERVATION);
    }

    public static AccessDeniedException userNotPermitted (String message) {
        return new AccessDeniedException(message, USER_NOT_PERMITTED);
    }


}
