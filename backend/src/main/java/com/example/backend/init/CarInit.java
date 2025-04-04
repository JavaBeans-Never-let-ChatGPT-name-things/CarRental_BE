package com.example.backend.init;

import com.example.backend.entity.CarBrandEntity;
import com.example.backend.entity.CarEntity;
import com.example.backend.entity.enums.CarState;
import com.example.backend.repository.CarBrandRepository;
import com.example.backend.repository.CarRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;


@Component
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Slf4j
public class CarInit implements CommandLineRunner {
    CarRepository carRepository;
    CarBrandRepository carBrandRepository;
    List<CarBrandEntity> carBrands =
            List.of(
                    CarBrandEntity.builder().name("BMW").logoUrl("https://res.cloudinary.com/daypnjdng/image/upload/v1743694275/bmw_hi0sc1.png").build(),
                    CarBrandEntity.builder().name("Mercedes").logoUrl("https://res.cloudinary.com/daypnjdng/image/upload/v1743694276/mercedes_uhcx7c.png").build(),
                    CarBrandEntity.builder().name("Audi").logoUrl("https://res.cloudinary.com/daypnjdng/image/upload/v1743694275/audi_alepws.png").build(),
                    CarBrandEntity.builder().name("Ford").logoUrl("https://res.cloudinary.com/daypnjdng/image/upload/v1743694275/ford_d6y2vo.png").build(),
                    CarBrandEntity.builder().name("Tesla").logoUrl("https://res.cloudinary.com/daypnjdng/image/upload/v1743694276/tesla_w69t9p.png").build(),
                    CarBrandEntity.builder().name("Land Rover").logoUrl("https://res.cloudinary.com/daypnjdng/image/upload/v1743694275/land_rover_hqaisf.png").build(),
                    CarBrandEntity.builder().name("Chevrolet").logoUrl("https://res.cloudinary.com/daypnjdng/image/upload/v1743694275/chevrolet_pzbv9d.png").build(),
                    CarBrandEntity.builder().name("Ferrari").logoUrl("https://res.cloudinary.com/daypnjdng/image/upload/v1743694607/ferrari_oqhfuy.png").build()
            );

    @Override
    public void run(String... args) throws Exception {
        log.info("CarInit is running");
        if (carBrandRepository.count() == 0) {
            carBrandRepository.saveAll(carBrands);
            log.info("Car brands initialized");
        }
        else{
            log.info("Car brands already exist");
        }
        if (carRepository.count() == 0) {
            List<String> brandImageUrls = List.of(
                    "https://res.cloudinary.com/daypnjdng/image/upload/v1743695601/car_audi_ugo1dy.jpg",   // Audi
                    "https://res.cloudinary.com/daypnjdng/image/upload/v1743695361/bmw_car_xdux7z.jpg",    // BMW
                    "https://res.cloudinary.com/daypnjdng/image/upload/v1743695602/chevrolet_car_cakyq8.png", // Chevrolet
                    "https://res.cloudinary.com/daypnjdng/image/upload/v1743695358/ford_car_gbvmjd.jpg",     // Ford
                    "https://res.cloudinary.com/daypnjdng/image/upload/v1743695358/mercedes_car_aezppg.png",  // Mercedes
                    "https://res.cloudinary.com/daypnjdng/image/upload/v1743695359/tesla_car_xl1j2z.jpg",     // Tesla
                    "https://res.cloudinary.com/daypnjdng/image/upload/v1743695358/ferrari_car_xtn0uy.jpg",  // Ferrari
                    "https://res.cloudinary.com/daypnjdng/image/upload/v1743695413/land_rover_car_kowp03.png" // Land Rover
            );

            List<String> audiCars = List.of("Audi A3", "Audi A4", "Audi A5", "Audi A6", "Audi A7", "Audi A8", "Audi Q3", "Audi Q5", "Audi Q7", "Audi Q8", "Audi R8", "Audi TT", "Audi e-tron", "Audi e-tron GT");
            List<String> bmwCars = List.of("BMW 3 Series", "BMW 5 Series", "BMW 7 Series", "BMW X1", "BMW X3", "BMW X4", "BMW X5", "BMW X6", "BMW X7", "BMW M3", "BMW M4", "BMW M5", "BMW Z4", "BMW i3", "BMW i4", "BMW iX");
            List<String> chevroletCars = List.of("Chevrolet Malibu", "Chevrolet Impala", "Chevrolet Camaro", "Chevrolet Corvette", "Chevrolet Equinox", "Chevrolet Traverse", "Chevrolet Tahoe", "Chevrolet Suburban", "Chevrolet Silverado", "Chevrolet Colorado");
            List<String> fordCars = List.of("Ford F-150", "Ford Mustang", "Ford Explorer", "Ford Escape", "Ford Edge", "Ford Expedition", "Ford Ranger", "Ford Bronco", "Ford Focus", "Ford Fusion");
            List<String> mercedesCars = List.of("Mercedes-Benz A-Class", "Mercedes-Benz C-Class", "Mercedes-Benz E-Class", "Mercedes-Benz S-Class", "Mercedes-Benz GLA", "Mercedes-Benz GLC", "Mercedes-Benz GLE", "Mercedes-Benz GLS", "Mercedes-Benz SLC", "Mercedes-Benz G-Class");
            List<String> teslaCars = List.of("Tesla Model S", "Tesla Model 3", "Tesla Model X", "Tesla Model Y", "Tesla Cybertruck");
            List<String> ferrariCars = List.of("Ferrari F8 Tributo", "Ferrari 488 GTB", "Ferrari Portofino", "Ferrari GTC4Lusso", "Ferrari 812 Superfast", "Ferrari LaFerrari", "Ferrari Roma");
            List<String> landRoverCars = List.of("Land Rover Range Rover", "Land Rover Range Rover Sport", "Land Rover Discovery", "Land Rover Defender", "Land Rover Velar", "Land Rover Evoque");

            List<List<String>> carLists = List.of(audiCars, bmwCars, chevroletCars, fordCars, mercedesCars, teslaCars, ferrariCars, landRoverCars);
            List<CarBrandEntity> carBrands = carBrandRepository.findAll();
            Random random = new Random();



            for (int i = 0; i < carLists.size(); i++) {
                List<String> cars = carLists.get(i);
                CarBrandEntity brand = carBrands.get(i);
                String carImageUrl = brandImageUrls.get(i);

                for (String carName : cars) {
                    CarEntity car = CarEntity.builder()
                            .id(carName)
                            .brand(brand)
                            .carImageUrl(carImageUrl)
                            .maxSpeed(100 + random.nextInt(301))
                            .carRange(10 + random.nextFloat(11))
                            .state(CarState.AVAILABLE)
                            .seatsNumber(2 + random.nextInt(5))
                            .rentalPrice(50 + random.nextFloat() * 450)
                            .engineType(random.nextBoolean() ? "Petrol" : "Electric")
                            .rating(0f)
                            .reviewsNum(0)
                            .build();
                    brand.addCar(car);
                    carRepository.save(car);
                    carBrandRepository.save(brand);
                }

            }

            log.info("Car data initialized");

        }
    }
}