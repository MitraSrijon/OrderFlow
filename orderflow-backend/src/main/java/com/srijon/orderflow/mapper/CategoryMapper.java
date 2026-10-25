package com.srijon.orderflow.mapper;

import com.srijon.orderflow.dto.CategoryDto.CategoryRequestDto;
import com.srijon.orderflow.dto.CategoryDto.CategoryResponseDto;
import com.srijon.orderflow.entity.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    //Converting the category request object to category
    public Category toEntity(CategoryRequestDto categoryRequestDto){

        Category category = new Category();

        category.setName(categoryRequestDto.getName());
        category.setDescription(categoryRequestDto.getDescription());

        return category;
    }

    //Converting the category to category response entity

    public CategoryResponseDto toResponseDto(Category category){

        CategoryResponseDto categoryResponseDto = new CategoryResponseDto();

        categoryResponseDto.setId(category.getId());
        categoryResponseDto.setName(category.getName());
        categoryResponseDto.setDescription(category.getDescription());
        categoryResponseDto.setCreated(category.getCreated());
        categoryResponseDto.setUpdated(category.getUpdated());

        return categoryResponseDto;
    }
}
