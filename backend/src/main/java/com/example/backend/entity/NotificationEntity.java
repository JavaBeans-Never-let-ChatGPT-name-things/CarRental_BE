package com.example.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "notifications")
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationEntity extends AbstractAuditing<Long> {
    @Id
    @JsonIgnore
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "title", nullable = false, columnDefinition = "TEXT")
    String title;

    @Column(name = "message", nullable = false, columnDefinition = "LONGTEXT")
    String message;

    @Column(name = "is_read", nullable = false)
    Boolean isRead = false;

    @Column(name = "image_url", columnDefinition = "TEXT")
    String imageUrl;

    @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JsonIgnore
    @JoinColumn(name = "account_id", nullable = false)
    AccountEntity account;
}
