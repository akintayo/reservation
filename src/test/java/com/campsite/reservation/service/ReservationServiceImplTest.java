package com.campsite.reservation.service;

import com.campsite.reservation.TestUtils;
import com.campsite.reservation.exception.AccessDeniedException;
import com.campsite.reservation.exception.InvalidFieldException;
import com.campsite.reservation.exception.ObjectNotFoundException;
import com.campsite.reservation.model.AvailableDate;
import com.campsite.reservation.model.Reservation;
import com.campsite.reservation.model.ReservationDTO;
import com.google.code.tempusfugit.concurrency.ConcurrentRule;
import com.google.code.tempusfugit.concurrency.RepeatingRule;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Rule;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Slf4j
@Execution(ExecutionMode.CONCURRENT)
class ReservationServiceImplTest {

    @Rule
    public ConcurrentRule concurrentRule = new ConcurrentRule();

    @Rule
    public RepeatingRule rule = new RepeatingRule();


    @BeforeAll
    public static void before() {
        TestUtils.startHazelCastEmbedded();
    }

    @Resource
    private ReservationService reservationService;

    @Test
    @Order(2)
    void retrieveAvailableDatesEmptyReservation() {
        LocalDate startDate = LocalDate.now().plusDays(2);
        LocalDate endDate = LocalDate.now().plusDays(4);
        List<AvailableDate> result =  reservationService.retrieveAvailableDates(startDate, endDate);
        assertEquals(1, result.size());
        assertEquals(startDate, result.get(0).getStartDate());
        assertEquals(endDate, result.get(0).getEndDate());
    }

    @Test
    void retrieveAvailableDatesSameArrivalDepartureDate() {
        LocalDate sameDate = LocalDate.now().plusDays(2);
        Assertions.assertThrows(InvalidFieldException.class, () -> {
            reservationService.retrieveAvailableDates(sameDate, sameDate);
        });
    }

    @Test
    void retrieveAvailableDatesDepartureDateBeforeArrivalDate() {
        LocalDate startDate = LocalDate.now().plusDays(3);
        LocalDate endDate = LocalDate.now().plusDays(2);
        Assertions.assertThrows(InvalidFieldException.class, () -> {
            reservationService.retrieveAvailableDates(startDate, endDate);
        });
    }

    @Test
    void retrieveAvailableDatesGreaterThan30Days() {
        LocalDate startDate = LocalDate.now().plusDays(31);
        LocalDate endDate = LocalDate.now().plusDays(34);
        Assertions.assertThrows(InvalidFieldException.class, () -> {
            reservationService.retrieveAvailableDates(startDate, endDate);
        });
    }

    @Test
    void makeReservationOverlap() {
        String email = RandomStringUtils.randomAlphanumeric(7).concat("@gg.com");
        LocalDate startDate = LocalDate.now().plusDays(3);
        LocalDate endDate = LocalDate.now().plusDays(4);
        ReservationDTO reservationDTO = new ReservationDTO();
        reservationDTO.setEmail(email);
        reservationDTO.setFullName("John Doe");
        reservationDTO.setCheckInDate(startDate);
        reservationDTO.setCheckoutDate(endDate);
        Assertions.assertThrows(AccessDeniedException.class, () -> {
            reservationService.createReservation(reservationDTO);
        });
    }

    @Test
    void cancelValidReservation() {
        String email = RandomStringUtils.randomAlphanumeric(7).concat("@gg.com");
        LocalDate startDate = LocalDate.now().plusDays(5);
        LocalDate endDate = LocalDate.now().plusDays(7);
        ReservationDTO reservationDTO = new ReservationDTO();
        reservationDTO.setEmail(email);
        reservationDTO.setFullName("John Doe");
        reservationDTO.setCheckInDate(startDate);
        reservationDTO.setCheckoutDate(endDate);
        Reservation reservation =  reservationService.createReservation(reservationDTO);
        reservationService.cancelReservation(reservation.getReservationId());
        try {
            reservationService.retrieveReservation(reservation.getReservationId());
        } catch (Exception ex) {
            assertTrue(ex instanceof ObjectNotFoundException);
        }
    }

    @Test
    void cancelInvalidReservation() {
        Assertions.assertThrows(ObjectNotFoundException.class, () -> {
            reservationService.cancelReservation("invalid");
        });
    }

    @Test
    @Rollback(false)
    void makeUserReservation() {
        String email = RandomStringUtils.randomAlphanumeric(7).concat("@gg.com");
        makeReservation( email);
    }

    @Test
    @Rollback(false)
    @Execution(ExecutionMode.CONCURRENT)
    void makeUserReservation2() {
        String email = RandomStringUtils.randomAlphanumeric(7).concat("@gg.com");
        makeReservation( email);
    }

    @Test
    @Rollback(false)
    void makeUserReservation3() {
        String email = RandomStringUtils.randomAlphanumeric(7).concat("@gg.com");
        makeReservation( email);
    }
    @Test
    @Rollback(false)
    void makeUserReservation4() {
        String email = RandomStringUtils.randomAlphanumeric(7).concat("@gg.com");
        makeReservation( email);
    }

    /**
     * Make reservations with unique email
     * @param email the unique email address
     */
    private Reservation makeReservation(String email) {
        LocalDate startDate = LocalDate.now().plusDays(2);
        LocalDate endDate = LocalDate.now().plusDays(4);
        ReservationDTO reservationDTO = new ReservationDTO();
        reservationDTO.setEmail(email);
        reservationDTO.setFullName("John Doe");
        reservationDTO.setCheckInDate(startDate);
        reservationDTO.setCheckoutDate(endDate);
        Reservation result =  reservationService.createReservation(reservationDTO);
        assertNotNull(result.getReservationId());
        assertEquals(startDate, result.getArrivalDate());
        assertEquals(endDate, result.getDepartureDate());
        assertNotNull(result.getUser());
        assertEquals(reservationDTO.getFullName(), result.getUser().getFullName());
        return result;
    }
}