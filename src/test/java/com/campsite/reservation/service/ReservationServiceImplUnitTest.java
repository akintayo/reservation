package com.campsite.reservation.service;

import com.campsite.reservation.exception.InvalidFieldException;
import com.campsite.reservation.model.AvailableDate;
import com.campsite.reservation.model.Reservation;
import com.campsite.reservation.model.ReservationDTO;
import com.campsite.reservation.repository.ReservationRepository;
import com.campsite.reservation.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class ReservationServiceImplUnitTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReservationService reservationService = new ReservationServiceImpl();

    @Test
    void firstSystemReservation() {
        LocalDate startDate = LocalDate.now().plusDays(2);
        LocalDate endDate = LocalDate.now().plusDays(4);
        ReservationDTO reservationDTO = new ReservationDTO();
        reservationDTO.setEmail("john@doe.com");
        reservationDTO.setFullName("John Doe");
        reservationDTO.setCheckInDate(startDate);
        reservationDTO.setCheckoutDate(endDate);
        when(reservationRepository.retrieveReservationForDates(any(), any())).thenReturn(
                Collections.emptyList());
        when(userRepository.findUserByEmail(reservationDTO.getEmail())).thenReturn(Optional.empty());
        Reservation result =  reservationService.createReservation(reservationDTO);
        assertNotNull(result.getReservationId());
        assertEquals(startDate, result.getArrivalDate());
        assertEquals(endDate, result.getDepartureDate());
        assertNotNull(result.getUser());
        assertEquals(reservationDTO.getFullName(), result.getUser().getFullName());
    }

    @Test
    void isAvailableSpots() {

        //given reserved spaces
        Reservation reservation = new Reservation();
        reservation.setArrivalDate(LocalDate.now().plusDays(3));
        reservation.setDepartureDate(LocalDate.now().plusDays(5));

        Reservation reservation2 = new Reservation();
        reservation2.setArrivalDate(LocalDate.now().plusDays(5));
        reservation2.setDepartureDate(LocalDate.now().plusDays(6));

        Reservation reservation3 = new Reservation();
        reservation3.setArrivalDate(LocalDate.now().plusDays(8));
        reservation3.setDepartureDate(LocalDate.now().plusDays(10));

        Reservation reservation4 = new Reservation();
        reservation4.setArrivalDate(LocalDate.now().plusDays(13));
        reservation4.setDepartureDate(LocalDate.now().plusDays(15));

        Reservation reservation5 = new Reservation();
        reservation5.setArrivalDate(LocalDate.now().plusDays(25));
        reservation5.setDepartureDate(LocalDate.now().plusDays(28));

        List<Reservation> reservedDays = Arrays.asList(reservation, reservation2, reservation3, reservation4, reservation5);


        //when requested is before any bookings
        LocalDate startDate = LocalDate.now().plusDays(2);
        LocalDate endDate = LocalDate.now().plusDays(3);
        when(reservationRepository.retrieveReservationForDates(any(), any())).thenReturn(
                reservedDays);
        List<AvailableDate> result = reservationService.retrieveAvailableDates(startDate, endDate);
        assertEquals(1, result.size());
        assertEquals(startDate, result.get(0).getStartDate());
        assertEquals(endDate, result.get(0).getEndDate());


        //when requested dates is on reserved bookings
        startDate = LocalDate.now().plusDays(3);
        endDate = LocalDate.now().plusDays(5);
        when(reservationRepository.retrieveReservationForDates(any(), any())).thenReturn(
                reservedDays);
        result = reservationService.retrieveAvailableDates(startDate, endDate);
        assertEquals(0, result.size());

        //when requested dates is between bookings
        startDate = LocalDate.now().plusDays(6);
        endDate = LocalDate.now().plusDays(8);
        when(reservationRepository.retrieveReservationForDates(any(), any())).thenReturn(
                reservedDays);
        result = reservationService.retrieveAvailableDates(startDate, endDate);
        assertEquals(1, result.size());
        assertEquals(startDate, result.get(0).getStartDate());
        assertEquals(endDate, result.get(0).getEndDate());

        //when

        startDate = LocalDate.now().plusDays(8);
        endDate = LocalDate.now().plusDays(18);
        when(reservationRepository.retrieveReservationForDates(any(), any())).thenReturn(
                reservedDays);
        result = reservationService.retrieveAvailableDates(startDate, endDate);
        assertEquals(1, result.size());
        assertTrue(endDate.isAfter(result.get(0).getEndDate()));

        //when check for reserved between overlapping available days

        startDate = LocalDate.now().plusDays(6);
        endDate = LocalDate.now().plusDays(13);
        when(reservationRepository.retrieveReservationForDates(any(), any())).thenReturn(
                reservedDays);
        result = reservationService.retrieveAvailableDates(startDate, endDate);
        assertEquals(2, result.size());
        assertTrue(startDate.isEqual(result.get(0).getStartDate()));
        assertTrue(endDate.isEqual(result.get(1).getEndDate()));

        // when check is at the end of reserved space
        startDate = LocalDate.now().plusDays(28);
        endDate = LocalDate.now().plusDays(30);
        when(reservationRepository.retrieveReservationForDates(any(), any())).thenReturn(
                reservedDays);
        result = reservationService.retrieveAvailableDates(startDate, endDate);
        assertEquals(1, result.size());
        assertTrue(startDate.isEqual(result.get(0).getStartDate()));
        assertTrue(endDate.isEqual(result.get(0).getEndDate()));

    }

    @Test
    void isAvailableSpotDepartureDateBeforeArrivalDate() {
        LocalDate startDate = LocalDate.now().plusDays(3);
        LocalDate endDate = LocalDate.now().plusDays(2);
        Assertions.assertThrows(InvalidFieldException.class, () -> {
            reservationService.retrieveAvailableDates(startDate, endDate);
        });
    }

    @Test
    void isAvailableSpotGreaterThan30Days() {
        LocalDate startDate = LocalDate.now().plusDays(31);
        LocalDate endDate = LocalDate.now().plusDays(34);
        Assertions.assertThrows(InvalidFieldException.class, () -> {
            reservationService.retrieveAvailableDates(startDate, endDate);
        });
    }
}