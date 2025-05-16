package com.example.backend.service;

import com.example.backend.entity.CarBrandEntity;
import com.example.backend.entity.CarEntity;
import com.example.backend.entity.RentalContractEntity;
import com.example.backend.entity.ReviewEntity;
import com.example.backend.entity.enums.CarState;
import com.example.backend.repository.CarBrandRepository;
import com.example.backend.repository.CarRepository;
import com.example.backend.repository.ContractRepository;
import com.example.backend.repository.ReviewRepository;
import com.example.backend.service.dto.CarDTO;
import com.example.backend.service.dto.ReviewDTO;
import com.example.backend.service.dto.request.AddCarRequestDTO;
import com.example.backend.service.dto.request.CarPageRequestDTO;
import com.example.backend.service.dto.response.CarSummaryDTO;
import com.example.backend.service.mapper.CarMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
@Slf4j
public class CarServiceImpl implements CarService{
    private final CarRepository carRepository;
    private final ContractRepository contractRepository;
    private final CarBrandRepository carBrandRepository;
    private final ReviewRepository reviewRepository;
    private final CloudinaryService cloudinaryService;
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

    @Override
    public String addCar(AddCarRequestDTO carDTO) {
        try
        {
            log.info("Adding car with ID: {}", carDTO);
            CarBrandEntity carBrand = carBrandRepository.findByName(carDTO.getBrandName())
                    .orElseThrow(() -> new RuntimeException("Car brand not found"));
            if (carDTO.getCarImage() != null)
            {
                String url = cloudinaryService.uploadFile(carDTO.getCarImage(), "car");
                log.info("Car image URL: {}", url);
                CarEntity carEntity = CarEntity.builder()
                        .carImageUrl(url)
                        .brand(carBrand)
                        .maxSpeed(carDTO.getMaxSpeed())
                        .carRange(carDTO.getCarRange())
                        .seatsNumber(carDTO.getSeatsNumber())
                        .rentalPrice(carDTO.getRentalPrice())
                        .engineType(carDTO.getEngineType())
                        .gearType(carDTO.getGearType())
                        .drive(carDTO.getDrive())
                        .id(carDTO.getId())
                        .state(CarState.AVAILABLE)
                        .reviewsNum(0)
                        .rating(0f)
                        .build();
                carRepository.save(carEntity);
                log.info("Car added successfully with ID: {}", carEntity.getId());
                return "Successfully added car " + carEntity.getId();
            }
            else
            {
                log.error("Car image is null for car ID: {}", carDTO.getId());
                return "Failed to add car " + carDTO.getId() + ": Car image is null";
            }
        }
        catch (Exception e)
        {
            log.error("Error adding car with ID: {}. Error: {}", carDTO.getId(), e.getMessage());
            return "Failed to add car " + carDTO.getId() + ": " + e.getMessage();
        }
    }

    @Override
    public List<CarSummaryDTO> top3CarsWithMostRentalCount() {
        List<Object[]> top3Cars = carRepository.findTop3ByRentalCount();
        return top3Cars.stream()
                .map(obj -> {
                    Long rentalCount = (Long) obj[0];
                    String id = (String) obj[1];
                    String imageUrl = (String) obj[4];
                    return CarSummaryDTO.builder()
                            .id(id)
                            .imageUrl(imageUrl)
                            .rentalCount(rentalCount)
                            .build();
                })
                .toList();
    }

    @Override
    public List<CarSummaryDTO> top3CarsWithMostRentedCountFromDateToDate(LocalDate startDate, LocalDate endDate) {
        List<Object[]> top3Cars = carRepository.findTop3ByRentalCountFromDateToDate(startDate, endDate);
        return top3Cars.stream()
                .map(objects -> {
                    String id = (String) objects[1];
                    Long rentalCount = (Long) objects[0];
                    String imageUrl = (String) objects[4];
                    return CarSummaryDTO.builder()
                            .id(id)
                            .imageUrl(imageUrl)
                            .rentalCount(rentalCount)
                            .build();
                }).toList();
    }

    @Override
    public List<CarSummaryDTO> top3CarsWithMostRating() {
        return carRepository.findTop3ByRating().stream()
                .map(obj ->
                    CarSummaryDTO.builder()
                            .id(obj.getId())
                            .imageUrl(obj.getCarImageUrl())
                            .rating(BigDecimal.valueOf(obj.getRating())
                                    .setScale(2, RoundingMode.HALF_UP)
                                    .doubleValue())
                            .rentalCount(0L)
                            .build()).toList();
    }

    @Override
    public List<CarSummaryDTO> top3CarsWithMostRatingFromDateToDate(LocalDate startDate, LocalDate endDate) {
        return carRepository.findTop3ByRatingFromDateToDate(startDate, endDate).stream()
                .map(objects -> {
                    String id = (String) objects[1];
                    double rating = ((BigDecimal) objects[0])
                            .setScale(2, RoundingMode.HALF_UP)
                            .doubleValue();
                    String imageUrl = (String) objects[4];
                    return CarSummaryDTO.builder()
                            .id(id)
                            .imageUrl(imageUrl)
                            .rating(rating)
                            .rentalCount(0L)
                            .build();
                }).toList();
    }
}
