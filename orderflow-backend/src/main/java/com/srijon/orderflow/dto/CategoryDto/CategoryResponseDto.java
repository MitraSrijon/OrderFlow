package com.srijon.orderflow.dto.CategoryDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class CategoryResponseDto {

    private Long id;

    private String name;

    private String description;

    private LocalDateTime created;

    private LocalDateTime updated;
}
