package com.srijon.orderflow.controller;

import com.srijon.orderflow.dto.OrderDto.OrderRequestDto;
import com.srijon.orderflow.dto.OrderDto.OrderResponseDto;
import com.srijon.orderflow.dto.OrderDto.OrderStatusUpdateDto;
import com.srijon.orderflow.enums.OrderStatus;
import com.srijon.orderflow.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasRole('CUSTOMER')")
    public OrderResponseDto placeOrder(
            @Valid @RequestBody OrderRequestDto requestDto) {
        return orderService.placeOrder(requestDto);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN' , 'CUSTOMER')")
    public OrderResponseDto getOrderById(@PathVariable Long id) {
        return orderService.getOrderById(id);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public OrderResponseDto updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody OrderStatusUpdateDto requestDto) {

        return orderService.updateOrderStatus(id, requestDto.getStatus());
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Page<OrderResponseDto> getAllOrders(Pageable pageable) {
        return orderService.getAllOrders(pageable);
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<OrderResponseDto> getOrdersByStatus(
            @PathVariable OrderStatus status,
            Pageable pageable) {

        return orderService.getOrdersByStatus(status, pageable);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('CUSTOMER')")
    public Page<OrderResponseDto> getMyOrders(
            Pageable pageable) {

        return orderService.getMyOrders(pageable);
    }

    @GetMapping("/date-range")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<OrderResponseDto> getOrdersByDateRange(
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate,
            Pageable pageable) {

        return orderService.getOrdersByDateRange(
                startDate, endDate, pageable
        );
    }
}