package com.srijon.orderflow.service;

import com.srijon.orderflow.dto.OrderDto.OrderRequestDto;
import com.srijon.orderflow.dto.OrderDto.OrderResponseDto;
import com.srijon.orderflow.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface OrderService {

    OrderResponseDto placeOrder(OrderRequestDto requestDto);

    OrderResponseDto getOrderById(Long id);

    OrderResponseDto updateOrderStatus(Long orderId, OrderStatus status);

    Page<OrderResponseDto> getAllOrders(Pageable pageable);

    Page<OrderResponseDto> getOrdersByStatus(OrderStatus status, Pageable pageable);

    Page<OrderResponseDto> getOrdersByCustomerEmail(String email, Pageable pageable);

    Page<OrderResponseDto> getOrdersByDateRange(
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    );
}