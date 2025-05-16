package com.example.backend.service;

import com.example.backend.service.dto.CarDTO;
import com.example.backend.service.dto.ReviewDTO;
import com.example.backend.service.dto.request.AddCarRequestDTO;
import com.example.backend.service.dto.request.CarPageRequestDTO;
import com.example.backend.service.dto.response.CarSummaryDTO;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;


public interface CarService {
    Long countAll();
    Long countByBrandId(Long brandId);
    Long countById(String id);
    List<CarDTO> findAll();
    Page<CarDTO> findAllWithPagination(CarPageRequestDTO carPageRequestDTO);
    Page<CarDTO> findByIdWithPaginationAndFilter(CarPageRequestDTO carPageRequestDTO, String id);
    Page<CarDTO> findByBrandIdWithPagination(CarPageRequestDTO carPageRequestDTO, Long brandId);
    List<ReviewDTO> findReviewsById(String id);

    CarDTO findById(String id);
    String addCar(AddCarRequestDTO carDTO);

    List<CarSummaryDTO> top3CarsWithMostRentalCount();
    List<CarSummaryDTO> top3CarsWithMostRentedCountFromDateToDate(LocalDate startDate, LocalDate endDate);

    List<CarSummaryDTO> top3CarsWithMostRating();
    List<CarSummaryDTO> top3CarsWithMostRatingFromDateToDate(LocalDate startDate, LocalDate endDate);
}
