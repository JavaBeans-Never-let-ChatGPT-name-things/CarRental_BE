package com.example.backend.controller;

import com.example.backend.entity.CarBrandEntity;
import com.example.backend.service.CarBrandService;
import com.example.backend.service.CarService;
import com.example.backend.service.dto.CarDTO;
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

    @GetMapping("/pagination")
    public Page<CarDTO> findAllWithPagination(@RequestBody CarPageRequestDTO carPageRequestDTO) {
        return carService.findAllWithPagination(carPageRequestDTO);
    }

    @GetMapping("/pagination/filter/{carId}")
    public Page<CarDTO> findAllWithPaginationAndFilter(@RequestBody CarPageRequestDTO carPageRequestDTO,@PathVariable("carId") String id) {
        return carService.findByIdWithPaginationAndFilter(carPageRequestDTO, id);
    }
    @GetMapping("/brands")
    public List<CarBrandEntity> findAllBrands() {
        return carBrandService.getAllCarBrands();
    }

    @GetMapping("/pagination/filter/brand/{brandId}")
    public Page<CarDTO> findByBrandIdWithPagination(@RequestBody CarPageRequestDTO carPageRequestDTO, @PathVariable("brandId") Long brandId) {
        return carService.findByBrandIdWithPagination(carPageRequestDTO, brandId);
    }
}
