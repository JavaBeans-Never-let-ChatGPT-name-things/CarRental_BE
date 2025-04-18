package com.example.backend.repository;

import com.example.backend.entity.RentalContractEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContractRepository extends JpaRepository<RentalContractEntity, Long> {
    List<RentalContractEntity> findAllByAccount_Username(String username);
}
