package com.srijon.orderflow.dto.ProductDto;

import com.srijon.orderflow.entity.Category;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class ProductRequestDto {

    @NotNull(message = "Category ID cannot be null")
    @Positive(message = "Category ID cannot be negative")
    private Long categoryId;

    @NotNull(message = "Name of the product cannot be blank")
    private String name;

    private String description;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be greater than 0")
    private BigDecimal price;

    @NotNull(message = "Stock is required")
    @PositiveOrZero(message = "Available stock cannot be negative")
    private Integer availableStock;
}
