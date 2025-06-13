package com.example.backend.service;

import com.example.backend.entity.AccountEntity;
import com.example.backend.entity.CarEntity;
import com.example.backend.entity.RentalContractEntity;
import com.example.backend.entity.ReviewEntity;
import com.example.backend.entity.enums.*;
import com.example.backend.repository.AccountRepository;
import com.example.backend.repository.CarRepository;
import com.example.backend.repository.ContractRepository;
import com.example.backend.service.dto.*;
import com.example.backend.service.dto.request.ContractRequestDTO;
import com.example.backend.service.dto.request.UpdateUserRequestDTO;
import com.example.backend.service.dto.response.UserSummaryDTO;
import com.example.backend.service.mapper.AccountMapper;
import com.example.backend.service.mapper.CarMapper;
import com.example.backend.service.mapper.ContractRequestMapper;
import com.example.backend.service.mapper.RentalContractMapper;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
@FieldDefaults(makeFinal = true)
public class AccountServiceImpl implements AccountService{
    private AccountRepository accountRepository;
    private CarRepository carRepository;
    private AccountMapper accountMapper;
    private CarMapper carMapper;
    private JwtService jwtService;
    private CloudinaryService cloudinaryService;
    private ContractRepository contractRepository;
    private RentalContractMapper rentalContractMapper;
    private ContractRequestMapper contractRequestMapper;
    public static Map<Long, String> pendingPayments = new ConcurrentHashMap<>();
    public static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private PayOsService payOsService;
    @Override
    public Optional<AccountDTO> findByUsername(String token) {
        String username = jwtService.extractUserName(token);
        if (username != null) {
            return accountRepository.findByUsername(username)
                    .map(accountMapper::toDto);
        }
        return Optional.empty();
    }

    @Override
    public void updateFavouriteCar(String carId, String token) {
        String username = jwtService.extractUserName(token);
        if (username != null) {
            AccountEntity accountEntity = accountRepository.findByUsername(username).orElseThrow(
                    () -> new RuntimeException("Account not found")
            );
            CarEntity carEntity = carRepository.findById(carId).orElseThrow(
                    () -> new RuntimeException("Car not found")
            );
            if (accountEntity.getFavouriteCars().contains(carEntity)) {
                accountEntity.removeFavouriteCar(carEntity);
            } else {
                accountEntity.addFavouriteCar(carEntity);
            }
            accountRepository.save(accountEntity);
            carRepository.save(carEntity);
        }
    }

    @Override
    public List<CarDTO> getFavouriteCars(String token) {
        String username = jwtService.extractUserName(token);
        if (username != null) {
            AccountEntity accountEntity = accountRepository.findByUsername(username).orElseThrow(
                    () -> new RuntimeException("Account not found")
            );

            return carRepository.findAllByIdIn(accountEntity.getFavouriteCars().stream().map(CarEntity::getId).toList())
                    .stream().map(carMapper::toDto).toList();
        }
        return List.of();
    }


    //Todo : check if email is already used, and send email to confirm
    @Override
    public void updateProfile(UpdateUserRequestDTO updateUserRequestDTO, String token) {
        AccountEntity account = accountRepository.findByUsername(jwtService.extractUserName(token)).orElseThrow(
                () -> new RuntimeException("Account not found")
        );
        try {
            if (updateUserRequestDTO.getAvatar() != null) {
                String url = cloudinaryService.uploadFile(updateUserRequestDTO.getAvatar(), "profile");
                account.setAvatarUrl(url);
            }
        account.setGender(updateUserRequestDTO.getGender());
        account.setDisplayName(updateUserRequestDTO.getDisplayName());
        account.setAddress(updateUserRequestDTO.getAddress());
        account.setPhoneNumber(updateUserRequestDTO.getPhoneNumber());
        account.setEmail(updateUserRequestDTO.getEmail());
        accountRepository.save(account);
        }
        catch(Exception e){
            log.info("Error uploading file: {}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public String rentCar(ContractRequestDTO contractRequestDTO, String token, String carId) {
        String username = jwtService.extractUserName(token);
        if (username != null) {
            AccountEntity accountEntity = accountRepository.findByUsername(username).orElseThrow(
                    () -> new RuntimeException("Account not found")
            );
            CarEntity carEntity = carRepository.findById(carId).orElseThrow(
                    () -> new RuntimeException("Car not found")
            );
            if (accountEntity.getAccountRole() != AccountRole.USER)
            {
                throw new RuntimeException("Only users can rent cars");
            }
            if (carEntity.getState() != CarState.AVAILABLE) {
                throw new RuntimeException("Car is not available");
            }
            RentalContractEntity rentalContractEntity = contractRequestMapper.toEntity(contractRequestDTO);
            rentalContractEntity.setContractStatus(ContractStatus.BOOKED);
            accountEntity.addContract(rentalContractEntity);
            carEntity.addContract(rentalContractEntity);
            rentalContractEntity.setAccount(accountEntity);
            rentalContractEntity.setCar(carEntity);
            carEntity.setState(CarState.RENTED);

            Long result = contractRepository.save(rentalContractEntity).getId();
            pendingPayments.put(result, carId);
            scheduler.schedule(() -> {
                if (pendingPayments.containsKey(result)) {
                    payOsService.paymentFailed(carId);
                }
            }, 5, TimeUnit.MINUTES);

            return "Successfully rented car with ID: " + carEntity.getId();
        }
        throw new RuntimeException("Token is invalid");
    }

    @Override
    public List<RentalContractDTO> getRentalContracts(String token) {
        String username = jwtService.extractUserName(token);
        if (username != null) {
            accountRepository.findByUsername(username).orElseThrow(
                    () -> new RuntimeException("Account not found")
            );
            return contractRepository.findAllByAccount_Username(username)
                    .stream().map(rentalContractMapper::toDto).toList();
        }
        return List.of();
    }

    @Override
    public void reviewRentalContract(Long rentalContractId, ReviewEntity entity) {
        RentalContractEntity rentalContractEntity = contractRepository.findById(rentalContractId).orElseThrow(
                () -> new RuntimeException("Rental contract not found")
        );
        AccountEntity account = accountRepository.findById(rentalContractEntity.getAccount().getId()).orElseThrow(
                () -> new RuntimeException("Account not found")
        );
        entity.setAccount(account);
        CarEntity car = carRepository.findById(rentalContractEntity.getCar().getId()).orElseThrow(
                () -> new RuntimeException("Car not found")
        );
        int star = entity.getStarsNum();
        float currentRating = car.getRating() * car.getReviewsNum();
        car.setReviewsNum(car.getReviewsNum() + 1);
        car.setRating((currentRating + star) / car.getReviewsNum());
        carRepository.save(car);
        rentalContractEntity.setContractStatus(ContractStatus.REVIEWED);
        rentalContractEntity.setReview(entity);
        contractRepository.save(rentalContractEntity);
    }

    @Override
    public List<UserDTO> searchAndSortUsers(String query, String sort, String status) {
        List<AccountEntity> users = accountRepository.findAllByAccountRoleNot(AccountRole.ADMIN);

        return users.stream()
                .filter(user -> user.getDisplayName().toLowerCase().contains(query.toLowerCase()))
                .filter(user -> {
                    if (status.equals("Active User")) return user.isEnabled();
                    else if (status.equals("Inactive User")) return !user.isEnabled();
                    else return true;
                })
                .sorted((u1, u2) -> switch (sort) {
                    case "Display Name" -> u1.getDisplayName().compareToIgnoreCase(u2.getDisplayName());
                    case "Contract Count" -> Long.compare(u2.getRentalContracts().size(), u1.getRentalContracts().size());
                    default -> 0;
                })
                .map(user -> UserDTO.builder()
                        .displayName(user.getDisplayName())
                        .email(user.getEmail())
                        .phoneNumber(user.getPhoneNumber())
                        .role(user.getAccountRole())
                        .enabled(user.isEnabled())
                        .avatarUrl(user.getAvatarUrl())
                        .gender(user.getGender())
                        .countractCount((long) ((user.getAccountRole() == AccountRole.USER)?(user.getRentalContracts().size()):
                                (user.getManagedContracts().size())))
                        .build()
                )
                .collect(Collectors.toList());
    }

    @Override
    public UserDetailDTO getUserDetail(String displayName) {
        AccountEntity accountEntity = accountRepository.findByDisplayName(displayName).orElseThrow(
                () -> new RuntimeException("Account not found")
        );
        List<RentalContractEntity> rentalContracts = contractRepository.findAllByAccount_Username(accountEntity.getUsername());
        float penaltyFee = rentalContracts.stream()
                .map(RentalContractEntity::getPenaltyFee)
                .reduce(0f, Float::sum);
        return UserDetailDTO.builder()
                .displayName(accountEntity.getDisplayName())
                .username(accountEntity.getUsername())
                .role(accountEntity.getAccountRole())
                .gender(accountEntity.getGender())
                .email(accountEntity.getEmail())
                .phoneNumber(accountEntity.getPhoneNumber())
                .totalPenalty(penaltyFee)
                .rentalContracts(rentalContracts.stream().map(rentalContractMapper::toDto).toList())
                .avatarUrl(accountEntity.getAvatarUrl())
                .build();
    }

    @Override
    public void upgradeUserRole(String displayName) {
        AccountEntity accountEntity = accountRepository.findByDisplayName(displayName).orElseThrow(
                () -> new RuntimeException("Account not found")
        );
        if (accountEntity.getAccountRole() == AccountRole.USER) {
            accountEntity.setAccountRole(AccountRole.EMPLOYEE);
            accountRepository.save(accountEntity);
        }
        else {
            throw new RuntimeException("Account is not a user");
        }
    }

    @Override
    public void downgradeUserRole(String displayName) {
        AccountEntity accountEntity = accountRepository.findByDisplayName(displayName).orElseThrow(
                () -> new RuntimeException("Account not found")
        );
        if (accountEntity.getAccountRole() == AccountRole.EMPLOYEE) {
            accountEntity.setAccountRole(AccountRole.USER);
            accountRepository.save(accountEntity);
        }
        else {
            throw new RuntimeException("Account is not an employee");
        }
    }

    @Override
    public List<String> getAvailableEmployees(Long contractId) {
        RentalContractEntity rentalContractEntity = contractRepository.findById(contractId).orElseThrow(
                () -> new RuntimeException("Rental contract not found")
        );
        return accountRepository.findAllAvailableEmployeesOnStartDate(rentalContractEntity.getStartDate())
                .stream().map(AccountEntity::getDisplayName).toList();
    }

    @Override
    public void verifyAccount(String token) {
        String username = jwtService.extractUserName(token);
        AccountEntity entity = accountRepository.findByUsername(username).orElseThrow(
                () -> new RuntimeException("Account not found")
        );
        if (entity.getPhoneNumber().isEmpty() || entity.getAddress().isEmpty())
        {
            throw new RuntimeException("Unqualified");
        }
    }

    @Override
    public List<UserSummaryDTO> top3BestUser() {
        return accountRepository.findAllByAccountRole(AccountRole.USER).stream()
                .sorted(Comparator.comparingDouble(user -> calculateUserScore((AccountEntity) user)).reversed())
                .limit(3)
                .map(account ->
                    UserSummaryDTO.builder()
                            .username(account.getUsername())
                            .gender(account.getGender())
                            .email(account.getEmail())
                            .displayName(account.getDisplayName())
                            .creditPoint(BigDecimal.valueOf(calculateUserScore(account))
                                    .setScale(2, RoundingMode.HALF_UP).doubleValue())
                            .build())
                .toList();
    }

    @Override
    public List<UserSummaryDTO> top3WorstUser() {
        return accountRepository.findAllByAccountRole(AccountRole.USER).stream()
                .sorted(Comparator.comparingDouble(this::calculateUserScore))
                .limit(3)
                .map(account ->
                        UserSummaryDTO.builder()
                                .username(account.getUsername())
                                .email(account.getEmail())
                                .gender(account.getGender())
                                .displayName(account.getDisplayName())
                                .creditPoint(BigDecimal.valueOf(calculateUserScore(account))
                                        .setScale(2, RoundingMode.HALF_UP).doubleValue())
                                .build())
                .toList();
    }

    @Override
    public List<UserSummaryDTO> top3BestUserFromDateToDate(LocalDate fromDate, LocalDate toDate) {
        return accountRepository.findAllByAccountRole(AccountRole.USER).stream()
                .filter(user -> user.getRentalContracts().stream()
                        .anyMatch(contract -> !contract.getStartDate().isBefore(fromDate) && !contract.getStartDate().isAfter(toDate)))
                .sorted(Comparator.comparingDouble(user -> calculateUserScore( (AccountEntity) user, fromDate, toDate)).reversed())
                .map(account ->
                        UserSummaryDTO.builder()
                                .username(account.getUsername())
                                .email(account.getEmail())
                                .gender(account.getGender())
                                .displayName(account.getDisplayName())
                                .creditPoint(BigDecimal.valueOf(calculateUserScore(account, fromDate, toDate))
                                        .setScale(2, RoundingMode.HALF_UP).doubleValue())
                                .build())
                .limit(3)
                .toList();
    }

    @Override
    public List<UserSummaryDTO> top3WorstUserFromDateToDate(LocalDate fromDate, LocalDate toDate) {
        return accountRepository.findAllByAccountRole(AccountRole.USER).stream()
                .filter(user -> user.getRentalContracts().stream()
                        .anyMatch(contract -> !contract.getStartDate().isBefore(fromDate) && !contract.getStartDate().isAfter(toDate)))
                .sorted(Comparator.comparingDouble(user -> calculateUserScore(user, fromDate, toDate)))
                .map(account ->
                        UserSummaryDTO.builder()
                                .username(account.getUsername())
                                .email(account.getEmail())
                                .gender(account.getGender())
                                .displayName(account.getDisplayName())
                                .creditPoint(BigDecimal.valueOf(calculateUserScore(account, fromDate, toDate))
                                        .setScale(2, RoundingMode.HALF_UP).doubleValue())
                                .build())
                .limit(3)
                .toList();
    }

    private double calculateUserScore(AccountEntity account) {
        long completedContracts = account.getRentalContracts().stream()
                .filter(c -> c.getContractStatus() == ContractStatus.COMPLETE || c.getContractStatus() == ContractStatus.REVIEWED)
                .count();

        long overdueContracts = account.getRentalContracts().stream()
                .filter(c -> c.getContractStatus() == ContractStatus.OVERDUE || c.getContractStatus() == ContractStatus.EXPIRED)
                .count();

        long intactReturns = account.getRentalContracts().stream()
                .filter(c -> c.getReturnCarStatus() == ReturnCarStatus.INTACT)
                .count();

        long damagedOrLostReturns = account.getRentalContracts().stream()
                .filter(c -> c.getReturnCarStatus() == ReturnCarStatus.DAMAGED || c.getReturnCarStatus() == ReturnCarStatus.NOT_RETURNED)
                .count();

        long notReturned = account.getRentalContracts().stream()
                .filter(c -> c.getReturnCarStatus() == ReturnCarStatus.LOST)
                .count();

        double averageRating = account.getRentalContracts().stream()
                .filter(c -> c.getReview() != null)
                .mapToDouble(c -> c.getReview().getStarsNum())
                .average()
                .orElse(0.0);

        long failedRetries = account.getRentalContracts().stream()
                .filter(c -> c.getRetryCountLeft() == 0)
                .count();

        long successPayments = account.getRentalContracts().stream()
                .filter(c -> c.getPaymentStatus().equals(PaymentStatus.SUCCESS))
                .count();

        return completedContracts * 5
                + intactReturns * 3
                + successPayments * 2
                + averageRating * 2
                - overdueContracts * 5
                - damagedOrLostReturns * 4
                - failedRetries * 2
                - notReturned * 10;
    }
    private double calculateUserScore(AccountEntity account, LocalDate fromDate, LocalDate toDate) {
        var contractsInRange = account.getRentalContracts().stream()
                .filter(c -> !c.getStartDate().isBefore(fromDate) && !c.getStartDate().isAfter(toDate))
                .toList();

        long completedContracts = contractsInRange.stream()
                .filter(c -> c.getContractStatus() == ContractStatus.COMPLETE || c.getContractStatus() == ContractStatus.REVIEWED)
                .count();

        long overdueContracts = contractsInRange.stream()
                .filter(c -> c.getContractStatus() == ContractStatus.OVERDUE || c.getContractStatus() == ContractStatus.EXPIRED)
                .count();

        long intactReturns = contractsInRange.stream()
                .filter(c -> c.getReturnCarStatus() == ReturnCarStatus.INTACT)
                .count();

        long damagedOrLostReturns = contractsInRange.stream()
                .filter(c -> c.getReturnCarStatus() == ReturnCarStatus.DAMAGED || c.getReturnCarStatus() == ReturnCarStatus.NOT_RETURNED)
                .count();

        long notReturned = contractsInRange.stream()
                .filter(c -> c.getReturnCarStatus() == ReturnCarStatus.LOST)
                .count();

        double averageRating = contractsInRange.stream()
                .filter(c -> c.getReview() != null)
                .mapToDouble(c -> c.getReview().getStarsNum())
                .average()
                .orElse(0.0);

        long failedRetries = contractsInRange.stream()
                .filter(c -> c.getRetryCountLeft() == 0)
                .count();

        long successPayments = contractsInRange.stream()
                .filter(c -> c.getPaymentStatus().equals(PaymentStatus.SUCCESS))
                .count();

        return completedContracts * 5
                + intactReturns * 3
                + successPayments * 2
                + averageRating * 2
                - overdueContracts * 5
                - damagedOrLostReturns * 4
                - failedRetries * 2
                - notReturned * 10;
    }
}
