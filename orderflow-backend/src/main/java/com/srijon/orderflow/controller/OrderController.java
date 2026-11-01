package com.srijon.orderflow.controller;

import com.srijon.orderflow.dto.OrderDto.OrderRequestDto;
import com.srijon.orderflow.dto.OrderDto.OrderResponseDto;
import com.srijon.orderflow.dto.OrderDto.OrderStatusUpdateDto;
import com.srijon.orderflow.enums.OrderStatus;
import com.srijon.orderflow.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponseDto placeOrder(
            @Valid @RequestBody OrderRequestDto requestDto) {
        return orderService.placeOrder(requestDto);
    }

    @GetMapping("/{id}")
    public OrderResponseDto getOrderById(@PathVariable Long id) {
        return orderService.getOrderById(id);
    }

    @PatchMapping("/{id}/status")
    public OrderResponseDto updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody OrderStatusUpdateDto requestDto) {

        return orderService.updateOrderStatus(id, requestDto.getStatus());
    }

    @GetMapping
    public Page<OrderResponseDto> getAllOrders(Pageable pageable) {
        return orderService.getAllOrders(pageable);
    }

    @GetMapping("/status/{status}")
    public Page<OrderResponseDto> getOrdersByStatus(
            @PathVariable OrderStatus status,
            Pageable pageable) {

        return orderService.getOrdersByStatus(status, pageable);
    }

    @GetMapping("/customer")
    public Page<OrderResponseDto> getOrdersByCustomerEmail(
            @RequestParam String email,
            Pageable pageable) {

        return orderService.getOrdersByCustomerEmail(email, pageable);
    }

    @GetMapping("/date-range")
    public Page<OrderResponseDto> getOrdersByDateRange(
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate,
            Pageable pageable) {

        return orderService.getOrdersByDateRange(
                startDate, endDate, pageable
        );
    }
}