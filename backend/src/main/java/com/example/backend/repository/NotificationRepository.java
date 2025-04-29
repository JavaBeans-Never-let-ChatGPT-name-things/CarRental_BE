package com.example.backend.repository;

import com.example.backend.entity.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {
     @Query(
             value = "SELECT * FROM notifications ORDER BY id DESC ",
                nativeQuery = true
     )
     List<NotificationEntity> findByAccountId(Long accountId);
}
