package com.example.backend.repository;

import com.example.backend.entity.FCMTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FCMRepository extends JpaRepository<FCMTokenEntity, Long> {
    List<FCMTokenEntity> findAllByUserId(Long id);
    Optional<FCMTokenEntity> findByToken(String token);
}
