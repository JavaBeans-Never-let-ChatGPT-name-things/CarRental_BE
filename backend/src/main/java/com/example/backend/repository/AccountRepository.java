package com.example.backend.repository;

import com.example.backend.entity.AccountEntity;
import com.example.backend.entity.enums.AccountRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<AccountEntity, Long> {
    AccountEntity findByUsername(String username);
    AccountEntity findByAccountRole (AccountRole accountRole);
    AccountEntity findByEmail(String email);
}
