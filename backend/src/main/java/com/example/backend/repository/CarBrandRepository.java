package com.example.backend.repository;

import com.example.backend.entity.CarBrandEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CarBrandRepository extends JpaRepository<CarBrandEntity, Long> {

}
