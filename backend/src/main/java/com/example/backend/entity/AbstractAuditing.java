package com.example.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

@Setter
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AbstractAuditing<ID> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @CreatedDate
    @JsonIgnore
    @Column(name = "created_date", updatable = false)
    public Instant createdDate = Instant.now();

    @LastModifiedDate
    @JsonIgnore
    @Column(name = "last_modified_date")
    public Instant lastModifiedDate = Instant.now();

    public abstract ID getId();

    @JsonIgnore
    @Transient
    public ZonedDateTime getCreatedDateAtGMT7(){
        return createdDate.atZone(ZoneOffset.UTC).withZoneSameInstant(ZoneId.of("Asia/Ho_Chi_Minh"));
    }

    @JsonIgnore
    @Transient
    public ZonedDateTime getLastModifiedDateAtGMT7(){
        return lastModifiedDate.atZone(ZoneOffset.UTC).withZoneSameInstant(ZoneId.of("Asia/Ho_Chi_Minh"));
    }
}
