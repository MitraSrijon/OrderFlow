package com.srijon.orderflow.dto.OrderDto;

import com.srijon.orderflow.dto.OrderItemDto.OrderItemRequestDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OrderRequestDto {

    @NotBlank(message = "An order must contain at least 1 order item")
    @Valid
    private List<OrderItemRequestDto> items;

}