
package com.It_Asset.it_asset_equipment_tracker.controller;

import com.It_Asset.it_asset_equipment_tracker.entity.Category;
import com.It_Asset.it_asset_equipment_tracker.repository.CategoryRepository;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryRepository categoryRepository;

    public CategoryController(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public record CategoryRequest(
            @NotBlank(message = "Category name is required")
            @Size(max = 100, message = "Category name cannot exceed 100 characters")
            String name
    ) {}

    // Get all categories.
    @GetMapping
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    // Create a category.
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public Category createCategory(
            @Valid @RequestBody CategoryRequest request) {

        String name = request.name().trim();
        checkDuplicateName(name, null);

        Category category = new Category(name);
        return categoryRepository.save(category);
    }

    // Update an existing category.
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Category updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Category not found with ID: " + id));

        String name = request.name().trim();
        checkDuplicateName(name, id);

        category.setName(name);
        return categoryRepository.save(category);
    }

    // Delete a category.
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable Long id) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Category not found with ID: " + id));

        categoryRepository.delete(category);
    }

    // Reject duplicate category names, ignoring the category being updated.
    private void checkDuplicateName(String name, Long excludedId) {
        boolean duplicateExists = categoryRepository.findAll().stream()
                .anyMatch(category ->
                        category.getName().equalsIgnoreCase(name)
                                && !category.getId().equals(excludedId));

        if (duplicateExists) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Category name already exists");
        }
    }
}
