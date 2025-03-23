package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Entity
@Table(name = "logout_token")
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class LogoutToken extends AbstractAuditing<String>{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    String token;
}
