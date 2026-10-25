
package com.srijon.orderflow.service.impl;

import com.srijon.orderflow.dto.CategoryDto.CategoryRequestDto;
import com.srijon.orderflow.dto.CategoryDto.CategoryResponseDto;
import com.srijon.orderflow.entity.Category;
import com.srijon.orderflow.mapper.CategoryMapper;
import com.srijon.orderflow.repository.CategoryRepository;
import com.srijon.orderflow.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper mapper;

    @Override
    public Page<CategoryResponseDto> getAllCategories(Pageable pageable) {
        return categoryRepository.findAll(pageable)
                .map(mapper::toResponseDto);
    }

    @Override
    public CategoryResponseDto getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Category not found"));

        return mapper.toResponseDto(category);
    }

    @Override
    public CategoryResponseDto createCategory(
            CategoryRequestDto categoryRequestDto) {

        String name = categoryRequestDto.getName().trim();

        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Category name already exists");
        }

        categoryRequestDto.setName(name);

        Category category = mapper.toEntity(categoryRequestDto);
        Category savedCategory = categoryRepository.save(category);

        return mapper.toResponseDto(savedCategory);
    }

    @Override
    public CategoryResponseDto updateCategory(
            Long id, CategoryRequestDto categoryRequestDto) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Category not found"));

        String name = categoryRequestDto.getName().trim();

        boolean duplicateExists = categoryRepository.existsByNameIgnoreCase(name);

        if (duplicateExists && !category.getName().equalsIgnoreCase(name)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Category name already exists");
        }

        category.setName(name);
        category.setDescription(categoryRequestDto.getDescription());

        Category updatedCategory = categoryRepository.save(category);

        return mapper.toResponseDto(updatedCategory);
    }

    @Override
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Category not found"));

        categoryRepository.delete(category);
    }
}
