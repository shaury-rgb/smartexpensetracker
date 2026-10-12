package com.smarttracker.category.repository;

import com.smarttracker.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    @Query("SELECT c FROM Category c WHERE c.isSystem = true OR c.user.id = :userId ORDER BY c.isSystem DESC, c.name ASC")
    List<Category> findAvailableCategories(@Param("userId") Long userId);

    Optional<Category> findByIdAndUserId(Long id, Long userId);

    boolean existsByNameIgnoreCaseAndUserId(String name, Long userId);

    boolean existsByNameIgnoreCaseAndIsSystemTrue(String name);

    @Query("SELECT c FROM Category c WHERE (c.user.id = :userId OR c.isSystem = true) AND c.id = :id")
    Optional<Category> findAvailableCategoryById(@Param("id") Long id, @Param("userId") Long userId);

    @Query("SELECT c FROM Category c WHERE (c.user.id = :userId OR c.isSystem = true) AND LOWER(c.name) = LOWER(:name)")
    Optional<Category> findAvailableCategoryByName(@Param("name") String name, @Param("userId") Long userId);

    @Transactional
    @Modifying
    @Query("DELETE FROM Category c WHERE c.user IS NOT NULL")
    void deleteCustomCategories();
}
