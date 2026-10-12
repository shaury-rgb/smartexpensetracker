package com.smarttracker.category.service;

import com.smarttracker.category.dto.CategoryRequestDto;
import com.smarttracker.category.dto.CategoryResponseDto;
import com.smarttracker.category.entity.Category;
import com.smarttracker.category.repository.CategoryRepository;
import com.smarttracker.common.exception.ResourceNotFoundException;
import com.smarttracker.expense.repository.ExpenseRepository;
import com.smarttracker.user.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;

    public CategoryService(CategoryRepository categoryRepository, ExpenseRepository expenseRepository) {
        this.categoryRepository = categoryRepository;
        this.expenseRepository = expenseRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponseDto> getAvailableCategories(Long userId) {
        return categoryRepository.findAvailableCategories(userId).stream()
                .map(CategoryResponseDto::fromEntity)
                .toList();
    }

    public CategoryResponseDto createCategory(User user, CategoryRequestDto request) {
        String name = request.getName().trim();

        if (categoryRepository.existsByNameIgnoreCaseAndIsSystemTrue(name)) {
            throw new IllegalArgumentException("A system category with name '" + name + "' already exists");
        }
        if (categoryRepository.existsByNameIgnoreCaseAndUserId(name, user.getId())) {
            throw new IllegalArgumentException("A custom category with name '" + name + "' already exists");
        }

        Category category = new Category(name, false, user);
        Category saved = categoryRepository.save(category);
        return CategoryResponseDto.fromEntity(saved);
    }

    public CategoryResponseDto updateCategory(Long id, Long userId, CategoryRequestDto request) {
        Category category = categoryRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Custom category not found with ID: " + id));

        String newName = request.getName().trim();
        if (!category.getName().equalsIgnoreCase(newName)) {
            if (categoryRepository.existsByNameIgnoreCaseAndIsSystemTrue(newName)) {
                throw new IllegalArgumentException("A system category with name '" + newName + "' already exists");
            }
            if (categoryRepository.existsByNameIgnoreCaseAndUserId(newName, userId)) {
                throw new IllegalArgumentException("A custom category with name '" + newName + "' already exists");
            }
            category.setName(newName);
        }

        Category updated = categoryRepository.save(category);
        return CategoryResponseDto.fromEntity(updated);
    }

    public void deleteCategory(Long id, Long userId) {
        Category category = categoryRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Custom category not found with ID: " + id));

        if (expenseRepository.existsByUserIdAndCategoryIgnoreCase(userId, category.getName())) {
            throw new IllegalArgumentException("Cannot delete category that is associated with existing expenses");
        }

        categoryRepository.delete(category);
    }
}
