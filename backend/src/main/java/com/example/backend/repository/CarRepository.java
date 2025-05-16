package com.example.backend.repository;

import com.example.backend.entity.CarEntity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

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
            countQuery = "SELECT COUNT(*) FROM cars WHERE brand_id = :brand_id%",
            nativeQuery = true
    )
    Page<CarEntity> findAllByBrand(@Param("brand_id")Long brandId, Pageable pageable);

    @Query(
            value = "SELECT * FROM cars where id IN (:ids)",
            countQuery = "SELECT COUNT(*) FROM cars where id IN (:ids)",
            nativeQuery = true
    )
    List<CarEntity> findAllByIdIn(Iterable<String> ids);
    @Query(
            value = "SELECT COUNT(*) FROM cars where brand_id = :brandId",
            nativeQuery = true
    )
    Long countAllByBrandId(Long brandId);

    @Query(
            value = "SELECT COUNT(*) FROM cars WHERE id LIKE %:id%",
            nativeQuery = true
    )
    Long countById(@Param("id") String id);

    @Query(
            value = "SELECT COUNT(rc.id), c.* " +
                    "FROM cars c " +
                    "JOIN rental_contracts rc ON c.id = rc.car_id " +
                    "GROUP BY c.id " +
                    "ORDER BY COUNT(rc.id) DESC " +
                    "LIMIT 3",
            nativeQuery = true
    )
    List<Object[]> findTop3ByRentalCount();

    @Query(
            value = "SELECT COUNT(rc.id), c.* " +
                    "FROM cars c " +
                    "JOIN rental_contracts rc ON c.id = rc.car_id " +
                    "WHERE rc.start_date BETWEEN :startDate AND :endDate " +
                    "GROUP BY c.id " +
                    "ORDER BY COUNT(rc.id) DESC " +
                    "LIMIT 3",
            nativeQuery = true
    )
    List<Object[]> findTop3ByRentalCountFromDateToDate(LocalDate startDate, LocalDate endDate);

    @Query(
            value = "SELECT c.* FROM cars c " +
                    "ORDER BY c.rating DESC " +
                    "LIMIT 3" ,
            nativeQuery = true
    )
    List<CarEntity> findTop3ByRating();

    @Query(
            value = "SELECT AVG(r.stars_num), c.* FROM cars c " +
                    "JOIN rental_contracts rc ON c.id = rc.car_id " +
                    "JOIN reviews r ON rc.review_id = r.id " +
                    "WHERE rc.start_date BETWEEN :startDate AND :endDate " +
                    "GROUP BY c.id " +
                    "ORDER BY AVG(r.stars_num) DESC " +
                    "LIMIT 3" ,
            nativeQuery = true
    )
    List<Object[]> findTop3ByRatingFromDateToDate(LocalDate startDate, LocalDate endDate);
}
