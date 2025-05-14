package com.example.backend.repository;

import com.example.backend.entity.CarBrandEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CarBrandRepository extends JpaRepository<CarBrandEntity, Long> {
    Optional<CarBrandEntity> findByName(String name);

    @Query (value = "SELECT c.name FROM CarBrandEntity c")
    List<String> findAllBrandName();
}
