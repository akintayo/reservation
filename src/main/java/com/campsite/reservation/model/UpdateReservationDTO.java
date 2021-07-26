package com.campsite.reservation.model;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * POJO for updating a reservation
 */
@Data
public class UpdateReservationDTO {

    @NotNull(message = "Booking reference cannot be null")
    private String bookingReferenceId;

    /**
     * the new check in date
     */
    private LocalDate checkInDate;

    /**
     * the new check out date
     */
    private LocalDate checkoutDate;
}
