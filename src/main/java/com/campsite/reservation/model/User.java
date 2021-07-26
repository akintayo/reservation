package com.campsite.reservation.model;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.HashSet;
import java.util.Set;

@Table(name = "user_info", indexes = @Index(columnList = "email", name = "idx_email"))
@Entity
@Setter
@Getter
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "user_info")
public class User extends BaseEntity {

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String fullName;

    @OneToMany(orphanRemoval = true, cascade = {CascadeType.ALL},
            mappedBy = "user", fetch = FetchType.LAZY)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "reservation")
    private Set<Reservation> reservations = new HashSet<>();
}
