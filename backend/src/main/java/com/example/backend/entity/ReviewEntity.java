package com.example.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Entity
@Table(name = "reviews")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReviewEntity extends AbstractAuditing<Long>{
    @Id
    @JsonIgnore
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "stars_num", nullable = false)
    int starsNum;

    @Column(name = "comment")
    @Lob
    String comment;
}
