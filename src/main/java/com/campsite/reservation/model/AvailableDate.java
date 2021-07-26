package com.campsite.reservation.model;

import lombok.Getter;

import java.time.LocalDate;

/**
 * POJO to hold available dates when a reservation check is made
 */
@Getter
public class AvailableDate {

    private final LocalDate startDate;

    private final LocalDate endDate;

    public AvailableDate(LocalDate startDate, LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

}
