package com.example.backend.service;

import com.example.backend.entity.CarBrandEntity;
import com.example.backend.repository.CarBrandRepository;
import com.example.backend.service.dto.BrandDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class CarBrandServiceImpl implements CarBrandService{
    private final CarBrandRepository carBrandRepository;
    private final CloudinaryService cloudinaryService;
    @Override
    public List<CarBrandEntity> getAllCarBrands() {
        return carBrandRepository.findAll();
    }

    @Override
    public String addCarBrand(BrandDTO brand) {
        List<String> existingBrands = carBrandRepository.findAllBrandName();
        if (existingBrands.contains(brand.getName())) {
            return "Car brand already exists";
        }
        try {
            if (brand.getLogo()!= null)
            {
                String url = cloudinaryService.uploadFile(brand.getLogo(), "brand");
                CarBrandEntity carBrandEntity = CarBrandEntity.builder()
                        .name(brand.getName())
                        .logoUrl(url)
                        .build();
                carBrandRepository.save(carBrandEntity);
                return "Car brand added successfully";
            }
            else
            {
                return "Logo is required";
            }
        } catch (Exception e) {
            return "Error adding car brand: " + e.getMessage();
        }
    }

    @Override
    public List<String> getAllBrandNames() {
        return carBrandRepository.findAllBrandName();
    }
}
