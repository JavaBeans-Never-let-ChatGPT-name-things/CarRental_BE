package com.example.backend.repository;

import com.example.backend.entity.AccountEntity;
import com.example.backend.entity.enums.AccountRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<AccountEntity, Long> {
    Optional<AccountEntity> findByUsername(String username);
    Optional<AccountEntity> findByEmail(String email);
    List<AccountEntity> findAllByAccountRoleNot(AccountRole accountRole);
    Optional<AccountEntity> findByDisplayName(String displayName);

    @Query(
            value = """
        SELECT a.* FROM accounts a
        WHERE a.role = 'EMPLOYEE'
        AND (
            NOT EXISTS (
                SELECT 1 FROM rental_contracts c
                WHERE c.employee_id = a.id AND c.start_date = :startDate
            )
        )
        """,
            nativeQuery = true
    )
    List<AccountEntity> findAllAvailableEmployeesOnStartDate(@Param("startDate") LocalDate startDate);


}
