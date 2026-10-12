package com.smarttracker.budget.repository;

import com.smarttracker.budget.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {

    @Query("SELECT b FROM Budget b LEFT JOIN FETCH b.category " +
            "WHERE b.user.id = :userId AND b.month = :month AND b.year = :year " +
            "ORDER BY b.category.name ASC NULLS FIRST")
    List<Budget> findByUserIdAndMonthAndYear(
            @Param("userId") Long userId,
            @Param("month") int month,
            @Param("year") int year
    );

    Optional<Budget> findByIdAndUserId(Long id, Long userId);

    Optional<Budget> findByUserIdAndMonthAndYearAndCategoryIsNull(Long userId, int month, int year);

    boolean existsByUserIdAndMonthAndYearAndCategoryId(Long userId, int month, int year, Long categoryId);

    boolean existsByUserIdAndMonthAndYearAndCategoryIsNull(Long userId, int month, int year);
}
