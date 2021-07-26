package com.campsite.reservation.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * POJO for reservation
 */
@Getter
@Setter
public class ReservationDTO {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @Email(message = "Please provide a valid email address")
    private String email;

    @NotNull(message = "Check in date is required")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate checkInDate;

    @NotNull(message = "Check out date is required")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate checkoutDate;
}
