package com.example.backend.repository;

import com.example.backend.entity.RentalContractEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContractRepository extends JpaRepository<RentalContractEntity, Long> {
}
