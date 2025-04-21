package com.example.backend.controller;

import com.example.backend.entity.CarBrandEntity;
import com.example.backend.entity.ReviewEntity;
import com.example.backend.service.CarBrandService;
import com.example.backend.service.CarService;
import com.example.backend.service.dto.CarDTO;
import com.example.backend.service.dto.ReviewDTO;
import com.example.backend.service.dto.request.CarPageRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cars")
@RequiredArgsConstructor
public class CarController {
    private final CarService carService;
    private final CarBrandService carBrandService;
    @GetMapping("/")
    public List<CarDTO> findAll() {
        return carService.findAll();
    }

    @PostMapping("/pagination")
    public List<CarDTO> findAllWithPagination(@RequestBody CarPageRequestDTO carPageRequestDTO) {
        return carService.findAllWithPagination(carPageRequestDTO).getContent();
    }

    @PostMapping("/pagination/filter/{carId}")
    public List<CarDTO> findAllWithPaginationAndFilter(@RequestBody CarPageRequestDTO carPageRequestDTO,@PathVariable("carId") String id) {
        return carService.findByIdWithPaginationAndFilter(carPageRequestDTO, id).getContent();
    }
    @GetMapping("/brands")
    public List<CarBrandEntity> findAllBrands() {
        return carBrandService.getAllCarBrands();
    }

    @PostMapping("/pagination/filter/brand/{brandId}")
    public List<CarDTO> findByBrandIdWithPagination(@RequestBody CarPageRequestDTO carPageRequestDTO, @PathVariable("brandId") Long brandId) {
        return carService.findByBrandIdWithPagination(carPageRequestDTO, brandId).getContent();
    }

    @GetMapping("/count")
    public Long countAll() {
        return carService.countAll();
    }
    @GetMapping("/count/brand/{brandId}")
    public Long countByBrandId(@PathVariable("brandId") Long brandId) {
        return carService.countByBrandId(brandId);
    }
    @GetMapping("/count/filter/{carId}")
    public Long countById(@PathVariable("carId") String id) {
        return carService.countById(id);
    }

    @GetMapping("/reviews/{carId}")
    public List<ReviewDTO> findAllReviewsById(@PathVariable("carId") String id) {
        return carService.findReviewsById(id);
    }

    @GetMapping("/{carId}")
    public CarDTO findCarById(@PathVariable("carId") String id) {
        return carService.findById(id);
    }
}
