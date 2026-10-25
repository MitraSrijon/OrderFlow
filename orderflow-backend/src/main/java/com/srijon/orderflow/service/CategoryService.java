package com.srijon.orderflow.service;

import com.srijon.orderflow.dto.CategoryDto.CategoryRequestDto;
import com.srijon.orderflow.dto.CategoryDto.CategoryResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CategoryService {

    Page<CategoryResponseDto> getAllCategories(Pageable pageable);

    CategoryResponseDto getCategoryById(Long id);

    CategoryResponseDto createCategory(CategoryRequestDto categoryRequestDto);

    CategoryResponseDto updateCategory(Long id, CategoryRequestDto categoryRequestDto);

    void deleteCategory(Long id);
}