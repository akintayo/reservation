package com.campsite.reservation.web;

import com.campsite.reservation.model.AvailableDate;
import com.campsite.reservation.model.Reservation;
import com.campsite.reservation.model.ReservationDTO;
import com.campsite.reservation.model.ReservationResponseDTO;
import com.campsite.reservation.model.UpdateReservationDTO;
import com.campsite.reservation.service.ReservationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.websocket.server.PathParam;
import java.time.LocalDate;
import java.util.List;

import static org.springframework.http.HttpStatus.OK;

/**
 * Rest API to handle reservations, cancellation and modifications
 */
@RestController
public class ReservationController {

    @Resource
    private ReservationService reservationService;

    /**
     * Returns the list of {@link AvailableDate}s
     * @param startDate optional arrival date in format yyyy-mm-dd. If not provided, the next day is used
     * @param endDate optional departure date in format yyyy-mm-dd. If not provided, the end date is 30 days from arrival date
     * @return the list of {@link AvailableDate}s
     */
    @GetMapping("/availability")
    @ResponseBody
    public ResponseEntity<List<AvailableDate>> checkReservation(@PathParam("startDate") LocalDate startDate, @PathParam("endDate") LocalDate endDate) {
        return new ResponseEntity<>(reservationService.retrieveAvailableDates(startDate, endDate), OK);
    }

    /**
     * Perform actual booking provided there is an available spot
     * @param reservationDTO the payload of the booking to be made
     * @return {@link ReservationResponseDTO} containing the booking Id (if successful)
     */
    @PostMapping("/book")
    @ResponseBody
    public ResponseEntity<ReservationResponseDTO> createReservation(@Valid @RequestBody ReservationDTO reservationDTO) {
        Reservation createdReservation =  reservationService.createReservation(reservationDTO);
        return getReservationResponse(createdReservation);
    }

    /**
     * Modifies an existing reservation (provided there's availability)
     * @param updateReservationDTO the payload to be used for update
     * @return {@link ReservationResponseDTO} containing the booking Id (if successful) amd modifed dates
     */
    @PutMapping("/modify")
    @ResponseBody
    public ResponseEntity<ReservationResponseDTO> modifyReservation(@Valid @RequestBody UpdateReservationDTO updateReservationDTO) {
        Reservation modifyReservation =  reservationService.modifyReservation(updateReservationDTO);
        return getReservationResponse(modifyReservation);
    }

    /**
     * Cancels an existing reservation
     * @param reservationId the unique booking reference
     */
    @DeleteMapping("/cancel/{reservationId}")
    public ResponseEntity<Void> cancelReservation(@PathVariable String reservationId) {
        reservationService.cancelReservation(reservationId);
        return new ResponseEntity<>(OK);
    }

    private ResponseEntity<ReservationResponseDTO> getReservationResponse(Reservation createdReservation) {
        ReservationResponseDTO responseDTO = new ReservationResponseDTO();
        responseDTO.setBookingReferenceId(createdReservation.getReservationId());
        responseDTO.setStatus(createdReservation.getReservationStatus().toString());
        responseDTO.setFullName(createdReservation.getUser().getFullName());
        return new ResponseEntity<>(responseDTO, OK);
    }
}
