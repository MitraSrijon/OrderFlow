package com.srijon.orderflow.dto.OrderDto;

import com.srijon.orderflow.dto.OrderItemDto.OrderItemRequestDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OrderRequestDto {

    @NotBlank(message = "Customer name is required")
    private String customerName;

    @NotBlank(message = "Customer email is required")
    @Email(message = "Please provide a valid email")
    private String customerEmail;

    @NotEmpty(message = "An order must contain at least one item")
    @Valid
    private List<OrderItemRequestDto> items;
}