package com.example.backend.repository;

import com.example.backend.entity.CarEntity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CarRepository extends JpaRepository<CarEntity, String> {
    @Query(
            value = "SELECT * FROM cars WHERE id LIKE %:id%",
            countQuery = "SELECT COUNT(*) FROM cars WHERE id LIKE %:id%",
            nativeQuery = true
    )
    Page<CarEntity> findAllById(@Param("id") String id, Pageable pageable);

    @Query(
            value = "SELECT * FROM cars WHERE brand_id = :brand_id%",
            nativeQuery = true
    )
    Page<CarEntity> findAllByBrand(@Param("brand_id")Long brandId, Pageable pageable);
}
