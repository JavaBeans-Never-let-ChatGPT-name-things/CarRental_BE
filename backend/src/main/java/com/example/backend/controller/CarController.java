package com.example.backend.controller;

import com.example.backend.entity.CarBrandEntity;
import com.example.backend.service.CarBrandService;
import com.example.backend.service.CarService;
import com.example.backend.service.dto.CarDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping("/brands")
    public List<CarBrandEntity> findAllBrands() {
        return carBrandService.getAllCarBrands();
    }
}
