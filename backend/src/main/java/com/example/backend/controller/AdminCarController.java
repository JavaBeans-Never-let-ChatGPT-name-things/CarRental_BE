package com.example.backend.controller;

import com.example.backend.service.CarBrandService;
import com.example.backend.service.CarService;
import com.example.backend.service.dto.BrandDTO;
import com.example.backend.service.dto.CarDTO;
import com.example.backend.service.dto.request.AddCarRequestDTO;
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
}
