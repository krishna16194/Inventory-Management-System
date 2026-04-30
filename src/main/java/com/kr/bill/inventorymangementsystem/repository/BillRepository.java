package com.kr.bill.inventorymangementsystem.repository;

import com.kr.bill.inventorymangementsystem.model.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for {@link Bill} entities.
 *
 * <p>Provides standard CRUD operations via {@link JpaRepository} plus
 * custom queries for filtering bills by date range and customer details.</p>
 */
@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {

    /**
     * Returns all bills whose transaction time falls within the given range,
     * ordered by transaction time descending (newest first).
     *
     * @param start the start of the date-time range (inclusive)
     * @param end   the end of the date-time range (inclusive)
     * @return list of bills in the specified range, newest first
     */
    List<Bill> findByTransactionTimeBetweenOrderByTransactionTimeDesc(LocalDateTime start, LocalDateTime end);

    List<Bill> findByOwnerUsernameAndTransactionTimeBetweenOrderByTransactionTimeDesc(String username, LocalDateTime start, LocalDateTime end);

    List<Bill> findByCustomerNameContainingIgnoreCaseOrderByTransactionTimeDesc(String name);

    List<Bill> findByOwnerUsernameAndCustomerNameContainingIgnoreCaseOrderByTransactionTimeDesc(String username, String name);

    List<Bill> findAllByOrderByTransactionTimeDesc();

    List<Bill> findByOwnerUsernameOrderByTransactionTimeDesc(String username);

    /**
     * Calculates the total revenue collected within a given date-time range.
     * Returns 0.0 when no bills exist for the period.
     *
     * @param start the start of the date-time range (inclusive)
     * @param end   the end of the date-time range (inclusive)
     * @return sum of {@code totalAmount} for all bills in the range
     */
    @Query("SELECT COALESCE(SUM(b.totalAmount), 0) FROM Bill b WHERE b.transactionTime BETWEEN :start AND :end")
    double sumTotalAmountByTransactionTimeBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(SUM(b.totalAmount), 0) FROM Bill b WHERE b.owner.username = :username AND b.transactionTime BETWEEN :start AND :end")
    double sumTotalAmountByOwnerAndTransactionTimeBetween(@Param("username") String username, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /**
     * Counts the number of bills created within a given date-time range.
     *
     * @param start the start of the date-time range (inclusive)
     * @param end   the end of the date-time range (inclusive)
     * @return number of bills in the range
     */
    long countByTransactionTimeBetween(LocalDateTime start, LocalDateTime end);

    long countByOwnerUsernameAndTransactionTimeBetween(String username, LocalDateTime start, LocalDateTime end);
}