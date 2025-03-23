package com.example.backend.repository;

import com.example.backend.entity.AccountEntity;
import com.example.backend.entity.enums.AccountRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<AccountEntity, Long> {
    Optional<AccountEntity> findByUsername(String username);
    Optional<AccountEntity> findByAccountRole (AccountRole accountRole);
    Optional<AccountEntity> findByEmail(String email);
}
