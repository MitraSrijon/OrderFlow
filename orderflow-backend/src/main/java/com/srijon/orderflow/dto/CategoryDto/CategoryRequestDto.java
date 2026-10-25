package com.srijon.orderflow.dto.CategoryDto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
public class CategoryRequestDto {

    @NotBlank(message = "Name is required")
    private String name;

    private String description;
}
