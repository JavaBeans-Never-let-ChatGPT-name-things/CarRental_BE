package com.example.backend.repository;

import com.example.backend.entity.RentalContractEntity;
import com.example.backend.entity.enums.ContractStatus;
import com.example.backend.entity.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ContractRepository extends JpaRepository<RentalContractEntity, Long> {
    @Query(
            value = "SELECT rc.* FROM rental_contracts rc " +
                    "JOIN accounts a ON rc.account_id = a.id " +
                    "WHERE a.username = :username ORDER BY rc.id DESC",
            nativeQuery = true
    )
    List<RentalContractEntity> findAllByAccount_Username(@Param("username") String username);
    List<RentalContractEntity> findAllByPendingIsTrueAndEmployee_Id(Long employeeId);
    List<RentalContractEntity> findAllByCar_Id(String carId);
    List<RentalContractEntity> findAllByContractStatusIsAndStartDateAfterAndPaymentStatusIs(ContractStatus contractStatus, LocalDate date, PaymentStatus paymentStatus);
    List<RentalContractEntity> findAllByEmployee_Username(String username);

    @Query(
            value = "SELECT MONTH(rc.start_date) AS month, " +
                    "SUM(CASE WHEN rc.contract_status IN ('COMPLETE', 'PICKED_UP', 'REVIEWED', 'OVERDUE') " +
                    "         THEN rc.total_price ELSE rc.deposit END) AS revenue " +
                    "FROM rental_contracts rc " +
                    "WHERE YEAR(rc.start_date) = :year " +
                    "GROUP BY MONTH(rc.start_date) " +
                    "ORDER BY month",
            nativeQuery = true
    )
    List<Object[]> findMonthlyRevenueByYear(@Param("year") int year);

    @Query(
            value = "SELECT MONTH(rc.start_date) AS month, " +
                    "SUM(rc.penalty_fee) AS penalty " +
                    "FROM rental_contracts rc " +
                    "WHERE YEAR(rc.start_date) = :year " +
                    "GROUP BY MONTH(rc.start_date) " +
                    "ORDER BY month",
            nativeQuery = true
    )
    List<Object[]> findMonthlyPenaltyByYear(@Param("year") int year);

    @Query(
            value = "SELECT SUM(rc.total_price) FROM rental_contracts rc" +
                    " WHERE rc.start_date BETWEEN :startDate AND :endDate ",
            nativeQuery = true
    )
    Double totalRevenueFromDateToDate(@Param("startDate") LocalDate startDate,@Param("endDate") LocalDate endDate);

    @Query(
            value = "SELECT SUM(rc.penalty_fee) FROM rental_contracts rc" +
                    " WHERE rc.start_date BETWEEN :startDate AND :endDate ",
            nativeQuery = true
    )
    Double totalPenaltyFromDateToDate(@Param("startDate") LocalDate startDate,@Param("endDate") LocalDate endDate);

    @Query(
            value = "SELECT SUM(rc.total_price) FROM rental_contracts rc",
            nativeQuery = true
    )
    Double totalRevenue();

    @Query(
            value = "SELECT SUM(rc.penalty_fee) FROM rental_contracts rc",
            nativeQuery = true
    )
    Double totalPenalty();
    @Query(value = "SELECT rc.contract_status, COUNT(*)" +
            " FROM rental_contracts rc" +
            " GROUP BY rc.contract_status",
            nativeQuery = true)
    List<Object[]> countContractsByStatus();

    @Query (value = "SELECT rc.contract_status, COUNT(*) " +
            " FROM rental_contracts rc" +
            " WHERE rc.start_date BETWEEN :startDate AND :endDate" +
            " GROUP BY rc.contract_status",
        nativeQuery = true)
    List<Object[]> countContractsByStatusFromDateToDate(@Param("startDate") LocalDate startDate,@Param("endDate") LocalDate endDate);

    @Query(
            value = "SELECT rc.return_car_status, COUNT(*)" +
                    " FROM rental_contracts rc" +
                    " WHERE rc.return_car_status IS NOT NULL" +
                    " GROUP BY rc.return_car_status",
            nativeQuery = true
    )
    List<Object[]> countContractsByReturnCarStatus();
    @Query (value = "SELECT rc.return_car_status, COUNT(*) " +
            " FROM rental_contracts rc" +
            " WHERE rc.start_date BETWEEN :startDate AND :endDate" +
            " AND rc.return_car_status IS NOT NULL" +
            " GROUP BY rc.return_car_status",
        nativeQuery = true)
    List<Object[]> countContractsByReturnCarStatusFromDateToDate(@Param("startDate") LocalDate startDate,@Param("endDate") LocalDate endDate);
}
