package com.campsite.reservation.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;
import java.time.OffsetDateTime;

/**
 * Base Model class that other POJO derive from
 */
@Setter
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    /**
     * DateTime of entity creation
     */
    @Column(name = "created_date", updatable = false, nullable = false)
    @CreatedDate
    private OffsetDateTime createdDate;


    /**
     * DateTime of entity update
     */
    @Column(name = "updated_date")
    @LastModifiedDate
    private OffsetDateTime updatedDate;
}
