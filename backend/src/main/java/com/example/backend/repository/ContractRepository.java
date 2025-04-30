package com.example.backend.repository;

import com.example.backend.entity.RentalContractEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContractRepository extends JpaRepository<RentalContractEntity, Long> {
    @Query(
            value = "SELECT rc.* FROM rental_contracts rc " +
                    "JOIN accounts a ON rc.account_id = a.id " +
                    "WHERE a.username = :username ORDER BY rc.id DESC",
            nativeQuery = true
    )
    List<RentalContractEntity> findAllByAccount_Username(@Param("username") String username);
    List<RentalContractEntity> findAllByCar_Id(String carId);
}
