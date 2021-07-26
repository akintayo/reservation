package com.campsite.reservation.service;

import com.campsite.reservation.exception.AccessDeniedException;
import com.campsite.reservation.exception.InvalidFieldException;
import com.campsite.reservation.exception.ObjectNotFoundException;
import com.campsite.reservation.model.AvailableDate;
import com.campsite.reservation.model.Reservation;
import com.campsite.reservation.model.ReservationDTO;
import com.campsite.reservation.model.UpdateReservationDTO;
import com.campsite.reservation.model.User;
import com.campsite.reservation.repository.ReservationRepository;
import com.campsite.reservation.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.campsite.reservation.model.Reservation.STATUS.ACTIVE;
import static java.time.temporal.ChronoUnit.DAYS;

@Transactional
@Slf4j
@Service
public class ReservationServiceImpl implements ReservationService {
    private static final String RESERVATION_DATE_CONFLICT = "Unable to find a spot for the dates provided";

    @Resource
    private  ReservationRepository reservationRepository;

    @Resource
    private UserRepository userRepository;

    private final Lock lock;

    public ReservationServiceImpl () {
        lock = new ReentrantLock(true);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Reservation createReservation(ReservationDTO reservationDTO) throws AccessDeniedException {
        validateDateRange(reservationDTO.getCheckInDate(), reservationDTO.getCheckoutDate());

        User user;
        Reservation reservation = null;
        Optional<User> userOptional = userRepository.findUserByEmail(reservationDTO.getEmail());
        if (userOptional.isEmpty()) {
            user = new User();
            user.setEmail(reservationDTO.getEmail());
            user.setFullName(reservationDTO.getFullName());
        } else {
            user = userOptional.get();
        }
        boolean isLockAcquired = false;
        try {
            isLockAcquired = lock.tryLock(20, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        if (isLockAcquired) {
            try {
                //try check one more time
                if (!isAvailableSpot(reservationDTO.getCheckInDate(), reservationDTO.getCheckoutDate())) {
                    throw AccessDeniedException.reservationConflict(RESERVATION_DATE_CONFLICT);
                }
                reservation = new Reservation();
                reservation.setReservationId(UUID.randomUUID().toString());
                reservation.setArrivalDate(reservationDTO.getCheckInDate());
                reservation.setDepartureDate(reservationDTO.getCheckoutDate());
                reservation.setReservationStatus(ACTIVE);
                user.getReservations().add(reservation);
                reservation.setUser(user);
                userRepository.saveAndFlush(user);
            } finally {
                lock.unlock();
            }
        }
        return reservation;
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void cancelReservation(String reservationId) throws ObjectNotFoundException {
        Reservation reservation = retrieveReservation(reservationId);
        //we can safely delete
        log.info("Deleting reservation {}", reservationId);
        reservationRepository.delete(reservation);
    }

    public Reservation retrieveReservation(String reservationId) {
        Optional<Reservation> reservation = reservationRepository.findReservationByReservationId(reservationId);
        if (reservation.isEmpty()) {
            throw new ObjectNotFoundException("Unable to find reservation with ID: " + reservationId);
        }
        return reservation.get();
    }

    @Override
    public Reservation modifyReservation(UpdateReservationDTO updateReservationDTO) throws AccessDeniedException, ObjectNotFoundException {
        Reservation reservation = retrieveReservation(updateReservationDTO.getBookingReferenceId());
        validateDateRange(updateReservationDTO.getCheckInDate(), updateReservationDTO.getCheckoutDate());
        if (!isAvailableSpot(updateReservationDTO.getCheckInDate(), updateReservationDTO.getCheckoutDate())) {
            throw AccessDeniedException.reservationConflict(RESERVATION_DATE_CONFLICT);
        }

        boolean isLockAcquired=false;
        try {
            isLockAcquired = lock.tryLock(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        if (isLockAcquired) {
            try {
                //try check one more time
                if (!isAvailableSpot(updateReservationDTO.getCheckInDate(), updateReservationDTO.getCheckoutDate())) {
                    throw AccessDeniedException.reservationConflict(RESERVATION_DATE_CONFLICT);
                }
                log.info("Updating the arrival date from {} to {} and checkout date from {} to {} ",
                        reservation.getArrivalDate(), updateReservationDTO.getCheckInDate(),
                        reservation.getDepartureDate(), updateReservationDTO.getCheckoutDate());
                reservation.setArrivalDate(updateReservationDTO.getCheckInDate());
                reservation.setDepartureDate(updateReservationDTO.getCheckoutDate());
                reservationRepository.saveAndFlush(reservation);
            } finally {
                lock.unlock();
            }
        }
        return reservation;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AvailableDate> retrieveAvailableDates(LocalDate preferredStartDate, LocalDate preferredEndDate) {
        if (preferredStartDate == null) {
            preferredStartDate = LocalDate.now().plusDays(1);
        }

        if (preferredEndDate == null) {
            preferredEndDate = LocalDate.now().plusMonths(1);
        }
        validateDateRange(preferredStartDate, preferredEndDate);
        List<AvailableDate> availableDates = new ArrayList<>();
        List<AvailableDate> requestedAvailableDates = new ArrayList<>();

        //lets look ahead just to cover all corner cases

        long temp = 30 - DAYS.between(LocalDate.now(), preferredStartDate);
        LocalDate forwardLookingEnd = preferredStartDate.plusDays(temp);
        List<Reservation> result = reservationRepository.retrieveReservationForDates(preferredStartDate.minusDays(30 - temp), forwardLookingEnd);

        if (result.isEmpty()) {
            availableDates.add(new AvailableDate(preferredStartDate, preferredEndDate));
            return availableDates;
        }

        //check beginning for a potential availability
        if (preferredEndDate.isEqual(result.get(0).getArrivalDate()) || preferredEndDate.isBefore(result.get(0).getArrivalDate())) {
            availableDates.add(new AvailableDate(preferredStartDate, preferredEndDate));
        }

        for (int i = 0; i < result.size() - 1; i++) {
            if (!result.get(i).getDepartureDate().isEqual(result.get(i + 1).getArrivalDate())) {
                availableDates.add(new AvailableDate(result.get(i).getDepartureDate(), result.get(i + 1).getArrivalDate()));
            }
        }
        //end of loop add the remaining
        if (result.get(result.size() - 1).getDepartureDate().isBefore(preferredEndDate)) {
            availableDates.add(new AvailableDate(result.get(result.size() - 1).getDepartureDate(), preferredEndDate));
        }

        //sanitize results to available range the user provided
        for (AvailableDate date : availableDates) {
            if ((preferredEndDate.isEqual(date.getEndDate()) || preferredEndDate.isAfter(date.getEndDate()))
                    && preferredStartDate.isBefore(date.getEndDate())) {
                requestedAvailableDates.add(date);
            }
        }
        return requestedAvailableDates;
    }

    /**
     * Check if provided date range to be reserved is available
     * @param startDate actual start date for the reservation
     * @param endDate actual end date for the reservation
     * @return true if the date range is available otherwise returns false
     */
    @Transactional(readOnly = true)
    boolean isAvailableSpot(final LocalDate startDate, final LocalDate endDate) {
        long days = DAYS.between(startDate, endDate);
        if (days > 3) {
            throw new InvalidFieldException("Reservation can be done for maximum of 3 days");
        }
        List<AvailableDate> availableDates = retrieveAvailableDates(startDate, endDate);
        for (AvailableDate availableDate : availableDates) {
            LocalDate availableEndDate = availableDate.getEndDate();
            LocalDate availableStartDate = availableDate.getStartDate();
            if (endDate.isBefore(availableEndDate) || endDate.isAfter(availableEndDate)) {
                return false;
            }
            //exact match
            if (startDate.isEqual(availableStartDate) && endDate.isEqual(availableEndDate)) {
                return true;
            }

            if (startDate.isBefore(availableEndDate) && (endDate.isBefore(availableEndDate) || endDate.isEqual(availableEndDate))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Validates the date range to ensure checkInDate is T+1 and checkoutDate not over a month
     * ALso validates that a maximum of 3 days can be booked
     *
     * @param checkInDate  the date of arrival
     * @param checkoutDate the date of departure
     */
    private void validateDateRange(LocalDate checkInDate, LocalDate checkoutDate) {
        if (checkInDate.isAfter(checkoutDate)) {
            throw new InvalidFieldException("Check in date cannot be after checkout date!");
        }
        if (checkInDate.equals(checkoutDate)) {
            throw new InvalidFieldException("Start date and end date cannot be the same");
        }
        LocalDate today = LocalDate.now();
        if (checkInDate.isEqual(today) || checkInDate.isBefore(today)) {
            throw new InvalidFieldException("Start date must be one day ahead of arrival");
        }
        long days = DAYS.between(today, checkInDate);
        if (days > 30) {
            throw new InvalidFieldException("Reservation cannot be more than 1 month away");
        }
    }
}
