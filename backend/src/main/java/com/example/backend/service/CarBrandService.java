package com.example.backend.service;

import com.example.backend.entity.CarBrandEntity;
import com.example.backend.service.dto.BrandDTO;

import java.util.List;

public interface CarBrandService {
    List<CarBrandEntity> getAllCarBrands();
    String addCarBrand(BrandDTO brand);
    List<String> getAllBrandNames();
}
