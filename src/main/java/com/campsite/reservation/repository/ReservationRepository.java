package com.campsite.reservation.repository;

import com.campsite.reservation.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation,Long> {

    @Query(" select res from Reservation res where res.arrivalDate >= :start " +
            "and res.departureDate <= :end order by res.arrivalDate asc ")
    List<Reservation> retrieveReservationForDates(LocalDate start, LocalDate end);

    Optional<Reservation> findReservationByReservationId(String reservationId);
}
