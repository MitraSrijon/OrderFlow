package com.srijon.orderflow.service;

import com.srijon.orderflow.dto.OrderDto.OrderRequestDto;
import com.srijon.orderflow.dto.OrderDto.OrderResponseDto;
import com.srijon.orderflow.enums.OrderStatus;

public interface OrderService {

    OrderResponseDto placeOrder(OrderRequestDto requestDto);

    OrderResponseDto getOrderById(Long id);

    OrderResponseDto updateOrderStatus(Long orderId, OrderStatus status);
}