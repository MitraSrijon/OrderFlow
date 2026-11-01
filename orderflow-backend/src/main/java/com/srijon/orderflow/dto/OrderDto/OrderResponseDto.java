package com.srijon.orderflow.dto.OrderDto;

import com.srijon.orderflow.dto.OrderItemDto.OrderItemResponseDto;
import com.srijon.orderflow.enums.OrderStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class OrderResponseDto {

    private Long id;
    private String customerName;
    private String customerEmail;
    private BigDecimal totalAmount;
    private OrderStatus orderStatus;
    private LocalDateTime createdAt;
    private List<OrderItemResponseDto> items;
}