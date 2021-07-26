package com.campsite.reservation;

import com.campsite.reservation.service.ReservationService;
import com.campsite.reservation.service.ReservationServiceImpl;
import com.campsite.reservation.util.ReservationHelper;
import com.campsite.reservation.web.ReservationController;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.rest.RepositoryRestMvcAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.config.BootstrapMode;

import java.time.OffsetDateTime;
import java.util.Optional;

@SpringBootApplication(exclude = RepositoryRestMvcAutoConfiguration.class)
@EnableJpaAuditing(dateTimeProviderRef = "auditingDateTimeProvider")
@ComponentScan(basePackages={"com.campsite"})
@EnableJpaRepositories(bootstrapMode = BootstrapMode.DEFERRED,
        basePackages={"com.campsite.reservation.repository"})
public class ReservationApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReservationApplication.class, args);
    }

    /**
     * Required for {@link OffsetDateTime} type
     */
    @Bean(name = "auditingDateTimeProvider")
    public DateTimeProvider dateTimeProvider() {
        return () -> Optional.of(OffsetDateTime.now());
    }

}
