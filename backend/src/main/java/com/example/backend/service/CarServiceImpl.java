package com.example.backend.service;

import com.example.backend.entity.CarEntity;
import com.example.backend.entity.RentalContractEntity;
import com.example.backend.entity.ReviewEntity;
import com.example.backend.repository.CarRepository;
import com.example.backend.repository.ContractRepository;
import com.example.backend.repository.ReviewRepository;
import com.example.backend.service.dto.CarDTO;
import com.example.backend.service.dto.ReviewDTO;
import com.example.backend.service.dto.request.CarPageRequestDTO;
import com.example.backend.service.mapper.CarMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CarServiceImpl implements CarService{
    private final CarRepository carRepository;
    private final ContractRepository contractRepository;
    private final ReviewRepository reviewRepository;
    private final CarMapper carMapper;

    @Override
    public Long countAll() {
        return carRepository.count();
    }

    @Override
    public Long countByBrandId(Long brandId) {
        return carRepository.countAllByBrandId(brandId);
    }

    @Override
    public Long countById(String id) {
        return carRepository.countById(id);
    }

    @Override
    public List<CarDTO> findAll() {
        return carMapper.toDto(carRepository.findAll());
    }

    @Override
    public Page<CarDTO> findAllWithPagination(CarPageRequestDTO carPageRequestDTO) {
        try
        {
            Pageable pageable = new CarPageRequestDTO().getPageable(carPageRequestDTO);
            Page<CarEntity> carEntityPage = carRepository.findAll(pageable);
            return carEntityPage.map(carMapper::toDto);
        }
        catch (RuntimeException e)
        {
            return Page.empty();
        }
    }

    @Override
    public Page<CarDTO> findByIdWithPaginationAndFilter(CarPageRequestDTO carPageRequestDTO, String id) {
        Pageable pageable = new CarPageRequestDTO().getPageable(carPageRequestDTO);
        Page<CarEntity> carEntityPage = carRepository.findAllById(id, pageable);
        return carEntityPage.map(carMapper::toDto);
    }

    @Override
    public Page<CarDTO> findByBrandIdWithPagination(CarPageRequestDTO carPageRequestDTO, Long brandId) {
        Pageable pageable = new CarPageRequestDTO().getPageable(carPageRequestDTO);
        Page<CarEntity> carEntityPage = carRepository.findAllByBrand(brandId, pageable);
        return carEntityPage.map(carMapper::toDto);
    }

    @Override
    public List<ReviewDTO> findReviewsById(String id) {
        List<RentalContractEntity> rentalContracts = contractRepository.findAllByCar_Id(id);
        if (rentalContracts != null && !rentalContracts.isEmpty()) {
            List<Long> reviewIds = rentalContracts.stream()
                    .map(RentalContractEntity::getReview)
                    .filter(Objects::nonNull)
                    .map(ReviewEntity::getId)
                    .toList();
                return reviewRepository.findAllById(reviewIds)
                        .stream()
                        .map(review -> ReviewDTO.builder()
                                .comment(review.getComment())
                                .starsNum(review.getStarsNum())
                                .accountDisplayName(review.getAccount().getDisplayName())
                                .avatarUrl(review.getAccount().getAvatarUrl())
                                .build())
                        .toList();
        }
        return List.of();
    }

    @Override
    public CarDTO findById(String id) {
        return carRepository.findById(id).map(carMapper::toDto).orElse(CarDTO.builder().build());
    }
}
