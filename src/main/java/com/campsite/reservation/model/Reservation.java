package com.campsite.reservation.model;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.time.LocalDate;

@Table(name = "camp_reservation", indexes = @Index(columnList = "reservationId", name = "idx_reservation_id"),
        uniqueConstraints = {
        @UniqueConstraint(columnNames = {"arrival_date", "departure_date"}, name = "uk_arrival_departure")
})
@Entity
@Setter
@Getter
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "reservation")
public class Reservation extends BaseEntity {

    public enum STATUS {
        RESERVED,
        CANCELLED,
        ACTIVE,
    }

    @Column
    private String reservationId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "arrival_date")
    private LocalDate arrivalDate;

    @Column(name = "departure_date")
    private LocalDate departureDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private STATUS reservationStatus;
}
