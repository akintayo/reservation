package com.campsite.reservation.model;

import com.campsite.reservation.model.Reservation.STATUS;
import lombok.Data;

import java.util.Date;

/**
 * Response POJO for successful reservation
 */
@Data
public class ReservationResponseDTO {

    /**
     * The unique booking reference ID
     */
    private String bookingReferenceId;

    private String fullName;

    private Date checkInDate;

    private Date checkoutDate;

    /**
     * The {@link STATUS} of the reservation
     */
    private String status;
}
