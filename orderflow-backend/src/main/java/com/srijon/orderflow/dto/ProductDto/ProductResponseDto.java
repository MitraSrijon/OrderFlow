package com.srijon.orderflow.dto.ProductDto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class ProductResponseDto {

    private Long id;

    private String name;

    private String description;

    private BigDecimal price;

    private Integer availableStock;

    private Long categoryId;

    private String categoryName;

    private LocalDateTime created;

    private LocalDateTime updated;
}
