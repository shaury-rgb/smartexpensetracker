package com.smarttracker.expense.repository;

import com.smarttracker.analytics.dto.CategorySpendingProjection;
import com.smarttracker.analytics.dto.DailySpendingProjection;
import com.smarttracker.analytics.dto.MonthlyExpenseStatsProjection;
import com.smarttracker.analytics.dto.MonthlyTrendProjection;
import com.smarttracker.expense.entity.Expense;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long>, JpaSpecificationExecutor<Expense> {

    Optional<Expense> findByIdAndUserId(Long id, Long userId);

    boolean existsByIdAndUserId(Long id, Long userId);

    boolean existsByUserIdAndCategoryIgnoreCase(Long userId, String category);

    boolean existsByCategoryIgnoreCase(String category);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e " +
            "WHERE e.user.id = :userId " +
            "AND EXTRACT(MONTH FROM e.expenseDate) = :month " +
            "AND EXTRACT(YEAR FROM e.expenseDate) = :year")
    BigDecimal calculateMonthlySpending(
            @Param("userId") Long userId,
            @Param("month") int month,
            @Param("year") int year
    );

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e " +
            "WHERE e.user.id = :userId " +
            "AND LOWER(e.category) = LOWER(:category) " +
            "AND EXTRACT(MONTH FROM e.expenseDate) = :month " +
            "AND EXTRACT(YEAR FROM e.expenseDate) = :year")
    BigDecimal calculateCategoryMonthlySpending(
            @Param("userId") Long userId,
            @Param("category") String category,
            @Param("month") int month,
            @Param("year") int year
    );

    @Query(value = "SELECT " +
            "COALESCE(SUM(amount), 0)::NUMERIC(12,2) AS totalSpending, " +
            "COUNT(*)::BIGINT AS expenseCount, " +
            "COALESCE(ROUND(AVG(amount), 2), 0)::NUMERIC(12,2) AS averageAmount, " +
            "MAX(amount)::NUMERIC(12,2) AS maxAmount, " +
            "MIN(amount)::NUMERIC(12,2) AS minAmount " +
            "FROM expenses " +
            "WHERE user_id = :userId " +
            "AND expense_date >= :startDate AND expense_date <= :endDate",
            nativeQuery = true)
    MonthlyExpenseStatsProjection getMonthlyStats(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT e.category AS categoryName, " +
            "COALESCE(SUM(e.amount), 0) AS totalSpending, " +
            "COUNT(e) AS transactionCount " +
            "FROM Expense e " +
            "WHERE e.user.id = :userId " +
            "AND e.expenseDate >= :startDate AND e.expenseDate <= :endDate " +
            "GROUP BY e.category " +
            "ORDER BY SUM(e.amount) DESC")
    List<CategorySpendingProjection> findCategorySpending(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query(value = "SELECT " +
            "EXTRACT(YEAR FROM expense_date)::INTEGER AS year, " +
            "EXTRACT(MONTH FROM expense_date)::INTEGER AS month, " +
            "COALESCE(SUM(amount), 0)::NUMERIC(12,2) AS totalSpending, " +
            "COUNT(*)::BIGINT AS transactionCount " +
            "FROM expenses " +
            "WHERE user_id = :userId " +
            "AND expense_date >= :startDate AND expense_date <= :endDate " +
            "GROUP BY EXTRACT(YEAR FROM expense_date), EXTRACT(MONTH FROM expense_date) " +
            "ORDER BY year ASC, month ASC",
            nativeQuery = true)
    List<MonthlyTrendProjection> findMonthlySpendingTrend(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query(value = "SELECT " +
            "expense_date AS expenseDate, " +
            "COALESCE(SUM(amount), 0)::NUMERIC(12,2) AS totalSpending, " +
            "COUNT(*)::BIGINT AS transactionCount " +
            "FROM expenses " +
            "WHERE user_id = :userId " +
            "AND expense_date >= :startDate AND expense_date <= :endDate " +
            "GROUP BY expense_date " +
            "ORDER BY expense_date ASC",
            nativeQuery = true)
    List<DailySpendingProjection> findDailySpending(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT e FROM Expense e " +
            "WHERE e.user.id = :userId " +
            "ORDER BY e.createdAt DESC, e.id DESC")
    List<Expense> findRecentExpenses(
            @Param("userId") Long userId,
            Pageable pageable
    );

    @Query("SELECT e FROM Expense e " +
            "WHERE e.user.id = :userId " +
            "AND e.expenseDate >= :startDate AND e.expenseDate <= :endDate " +
            "ORDER BY e.amount DESC, e.id DESC")
    List<Expense> findTopExpenses(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );
}
