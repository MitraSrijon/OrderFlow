package com.srijon.orderflow.service.impl;

import com.srijon.orderflow.dto.OrderItemDto.OrderItemRequestDto;
import com.srijon.orderflow.dto.OrderDto.OrderResponseDto;
import com.srijon.orderflow.dto.OrderDto.OrderRequestDto;
import com.srijon.orderflow.entity.OrderEntities.Order;
import com.srijon.orderflow.entity.OrderEntities.OrderItem;
import com.srijon.orderflow.entity.Product;
import com.srijon.orderflow.enums.OrderStatus;
import com.srijon.orderflow.mapper.OrderMapper;
import com.srijon.orderflow.repository.OrderRepository;
import com.srijon.orderflow.repository.ProductRepository;
import com.srijon.orderflow.service.OrderService;
import com.srijon.orderflow.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;
    private final OrderMapper orderMapper;

    @Transactional
    @Override
    public OrderResponseDto placeOrder(OrderRequestDto requestDto) {

        //If client sends 2 request for same product it sums  up the quantity
        Map<Long, Integer> consolidatedItems = requestDto.getItems()
                .stream()
                .collect(Collectors.toMap(
                        OrderItemRequestDto::getProductId,
                        OrderItemRequestDto::getQuantity,
                        Integer::sum
                ));

        Order order = new Order();
        order.setCustomerName(requestDto.getCustomerName());
        order.setCustomerEmail(requestDto.getCustomerEmail());
        order.setOrderStatus(OrderStatus.PENDING);

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (Map.Entry<Long, Integer> item : consolidatedItems.entrySet()) {

            Long productId = item.getKey();
            Integer quantity = item.getValue();

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Product not found: " + productId
                    ));

            productService.deductStock(product.getId(), quantity);

            BigDecimal unitPrice = product.getPrice();
            BigDecimal subtotal = unitPrice.multiply(
                    BigDecimal.valueOf(quantity)
            );

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(quantity);
            orderItem.setUnitPrice(unitPrice);
            orderItem.setSubtotal(subtotal);

            orderItems.add(orderItem);
            totalAmount = totalAmount.add(subtotal);
        }

        order.setOrderItems(orderItems);
        order.setTotalAmount(totalAmount);

        Order savedOrder = orderRepository.save(order);

        return orderMapper.toOrderResponseDto(savedOrder);
    }

    @Transactional(readOnly = true)
    @Override
    public OrderResponseDto getOrderById(Long id) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Order not found"
                ));

        return orderMapper.toOrderResponseDto(order);
    }


    @Override
    public OrderResponseDto updateOrderStatus(Long orderId, OrderStatus status) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Order not found"
                ));

        if(order.getOrderStatus() == OrderStatus.CANCELLED){
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cancelled orders cant be updated"
            );
        }

        if(status == OrderStatus.CANCELLED){

            for(OrderItem item : order.getOrderItems()){
                productService.deductStock(
                        item.getProduct().getId(),
                        item.getQuantity()
                );
            }
        }

        order.setOrderStatus(status);

        Order updatedOrder = orderRepository.save(order);

        return orderMapper.toOrderResponseDto(updatedOrder);
    }
}