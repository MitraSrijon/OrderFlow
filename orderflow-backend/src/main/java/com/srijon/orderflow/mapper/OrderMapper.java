package com.srijon.orderflow.mapper;

import com.srijon.orderflow.dto.OrderItemDto.OrderItemResponseDto;
import com.srijon.orderflow.dto.OrderDto.OrderResponseDto;
import com.srijon.orderflow.entity.OrderEntities.Order;
import com.srijon.orderflow.entity.OrderEntities.OrderItem;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderMapper {

    public OrderItemResponseDto toOrderItemResponseDto(OrderItem orderItem) {
        OrderItemResponseDto dto = new OrderItemResponseDto();

        dto.setProductId(orderItem.getProduct().getId());
        dto.setProductName(orderItem.getProduct().getName());
        dto.setQuantity(orderItem.getQuantity());
        dto.setUnitPrice(orderItem.getUnitPrice());
        dto.setSubtotal(orderItem.getSubtotal());

        return dto;
    }

    public OrderResponseDto toOrderResponseDto(Order order) {
        OrderResponseDto dto = new OrderResponseDto();

        dto.setId(order.getId());
        dto.setCustomerName(order.getCustomerName());
        dto.setCustomerEmail(order.getCustomerEmail());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setOrderStatus(order.getOrderStatus());
        dto.setCreatedAt(order.getCreatedAt());

        List<OrderItemResponseDto> items = order.getOrderItems()
                .stream()
                .map(this::toOrderItemResponseDto)
                .toList();

        dto.setItems(items);

        return dto;
    }
}