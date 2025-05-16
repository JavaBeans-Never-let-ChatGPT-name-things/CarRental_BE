package com.example.backend.controller;

import com.example.backend.service.CarBrandService;
import com.example.backend.service.CarService;
import com.example.backend.service.dto.BrandDTO;
import com.example.backend.service.dto.request.AddCarRequestDTO;
import com.example.backend.service.dto.request.ReportRequestDTO;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/cars")
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class AdminCarController {
    CarBrandService carBrandService;
    CarService carService;
    @PostMapping(path = "/add/carBrand", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addCarBrand(@ModelAttribute BrandDTO brandDTO) {
        try
        {
            return ResponseEntity.ok().body(Map.of("message", carBrandService.addCarBrand(brandDTO)));
        }
        catch (Exception e)
        {
            return ResponseEntity.badRequest().body(Map.of("message",e.getMessage()));
        }

    }
    @PostMapping(path = "/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addCar(@ModelAttribute AddCarRequestDTO carDTO) {
        try
        {
            return ResponseEntity.ok().body(Map.of("message", carService.addCar(carDTO)));
        }
        catch (Exception e)
        {
            return ResponseEntity.badRequest().body(Map.of("message",e.getMessage()));
        }
    }

    @GetMapping("/top-3-rented")
    public ResponseEntity<?> getTop3RentedCars() {
        try
        {
            return ResponseEntity.ok().body(carService.top3CarsWithMostRentalCount());
        }
        catch (Exception e)
        {
            return ResponseEntity.badRequest().body(Map.of("message",e.getMessage()));
        }
    }
    @PostMapping("/top-3-rented-from-date-to-date")
    public ResponseEntity<?> getTop3RentedCarsFromDateToDate(@RequestBody ReportRequestDTO requestDTO) {
        try
        {
            return ResponseEntity.ok().body(carService.top3CarsWithMostRentedCountFromDateToDate(
                    requestDTO.getStartDate(),
                    requestDTO.getEndDate()
            ));
        }
        catch (Exception e)
        {
            return ResponseEntity.badRequest().body(Map.of("message",e.getMessage()));
        }
    }

    @GetMapping("/top-3-rating")
    public ResponseEntity<?> getTop3MostRatingCar() {
        try
        {
            return ResponseEntity.ok().body(carService.top3CarsWithMostRating());
        }
        catch (Exception e)
        {
            return ResponseEntity.badRequest().body(Map.of("message",e.getMessage()));
        }
    }

    @PostMapping("/top-3-rating-from-date-to-date")
    public ResponseEntity<?> getTop3MostRatingCarFromDateToDate(@RequestBody ReportRequestDTO requestDTO) {
        try
        {
            return ResponseEntity.ok().body(carService.top3CarsWithMostRatingFromDateToDate(
                    requestDTO.getStartDate(),
                    requestDTO.getEndDate()
            ));
        }
        catch (Exception e)
        {
            return ResponseEntity.badRequest().body(Map.of("message",e.getMessage()));
        }
    }
}
