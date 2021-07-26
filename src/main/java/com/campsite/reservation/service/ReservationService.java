package com.campsite.reservation.service;

import com.campsite.reservation.exception.AccessDeniedException;
import com.campsite.reservation.exception.InvalidFieldException;
import com.campsite.reservation.exception.ObjectNotFoundException;
import com.campsite.reservation.model.AvailableDate;
import com.campsite.reservation.model.Reservation;
import com.campsite.reservation.model.ReservationDTO;
import com.campsite.reservation.model.UpdateReservationDTO;

import java.time.LocalDate;
import java.util.List;

public interface ReservationService {

    /**
     * Reserves the campsite spot for the User
     * @param reservationDTO the reservation request to be created
     * @return the {@link Reservation} if there's an available spot within the provided time range
     * @throws AccessDeniedException if there's no vacant spot for the provided duration
     * @throws InvalidFieldException if the duration is more than 3 days or
     * reservation starts on current day or earlier than a month
     */
    Reservation createReservation(ReservationDTO reservationDTO) throws AccessDeniedException, InvalidFieldException;

    /**
     * Cancels a reserved campsite spot for the User
     * @param reservationId the unique reservation id for cancellation
     * @throws ObjectNotFoundException if no record can be found for the provided bookingReferenceId
     */
    void cancelReservation(String reservationId) throws ObjectNotFoundException;

    /**
     * Retrieves the reservation for the provider ID
     * @param reservationId the {@link Reservation} to retrieve
     * @return the reservation for the provider ID
     * @throws ObjectNotFoundException if no reservation can be found for the provided ID
     */
    Reservation retrieveReservation(String reservationId) throws ObjectNotFoundException;

    /**
     * Modifies the reserved campsite spot for the User
     * @param updateReservationDTO the payload for the reservation update
     * @return the {@link Reservation} if the operation was successful
     * @throws AccessDeniedException if there's no vacant spot for the provided duration or
     * the booking reference does not belong to the user
     * @throws ObjectNotFoundException if no record can be found for the provided bookingReferenceId
     */
    Reservation modifyReservation(UpdateReservationDTO updateReservationDTO) throws AccessDeniedException, ObjectNotFoundException;

    /**
     * Returns a list of {@link AvailableDate} for the campsite. Some constraints in this method are as follows
     * <ol>
     *     <li>
     *         Where startDate is not provided the startDate will be next day
     *     </li>
     *     <li>
     *         Where endDate is not provided, the endDate will be a month a way
     *     </li>
     *     <li>
     *         Where both startDate and endDate are not provided, the startDate will be next day and endDate; a month a way
     *     </li>
     * </ol>
     * @param startDate the start date to start query from
     * @param endDate the end date for the query
     * @return the list of {@link AvailableDate}
     * @throws InvalidFieldException if the supplied date range is invalid
     */
    List<AvailableDate> retrieveAvailableDates(LocalDate startDate, LocalDate endDate) throws InvalidFieldException;

}
