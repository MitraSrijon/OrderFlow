package com.srijon.orderflow;

import com.srijon.orderflow.service.ProductService;
import com.srijon.orderflow.service.impl.OrderServiceImpl;

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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductService productService;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderServiceImpl orderService;

    // --------------------------------------------------
    // PLACE ORDER TESTS
    // --------------------------------------------------

    @Test
    void shouldPlaceOrderSuccessfully() {

        Product product = createProduct(1L, "Headphones", "500.00", 10);

        OrderRequestDto request = createOrderRequest(1L, 2);

        OrderResponseDto expectedResponse = new OrderResponseDto();

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(orderMapper.toOrderResponseDto(any(Order.class)))
                .thenReturn(expectedResponse);

        OrderResponseDto response = orderService.placeOrder(request);

        assertNotNull(response);

        verify(productService).deductStock(1L, 2);
        verify(orderRepository).save(any(Order.class));
        verify(orderMapper).toOrderResponseDto(any(Order.class));
    }

    @Test
    void shouldCalculateOrderTotalCorrectly() {

        Product product = createProduct(1L, "Headphones", "500.00", 10);

        OrderRequestDto request = createOrderRequest(1L, 3);

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> {
                    Order savedOrder = invocation.getArgument(0);

                    assertEquals(
                            new BigDecimal("1500.00"),
                            savedOrder.getTotalAmount()
                    );

                    return savedOrder;
                });

        when(orderMapper.toOrderResponseDto(any(Order.class)))
                .thenReturn(new OrderResponseDto());

        orderService.placeOrder(request);

        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void shouldConsolidateDuplicateProductsInOrder() {

        Product product = createProduct(1L, "Headphones", "500.00", 10);

        OrderRequestDto request = new OrderRequestDto();
        request.setCustomerName("John");
        request.setCustomerEmail("john@example.com");

        OrderItemRequestDto item1 = createOrderItemRequest(1L, 2);
        OrderItemRequestDto item2 = createOrderItemRequest(1L, 3);

        request.setItems(List.of(item1, item2));

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(orderMapper.toOrderResponseDto(any(Order.class)))
                .thenReturn(new OrderResponseDto());

        orderService.placeOrder(request);

        verify(productRepository, times(1)).findById(1L);
        verify(productService).deductStock(1L, 5);
    }

    @Test
    void shouldThrowExceptionWhenProductDoesNotExist() {

        OrderRequestDto request = createOrderRequest(999L, 2);

        when(productRepository.findById(999L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> orderService.placeOrder(request)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void shouldNotSaveOrderWhenStockDeductionFails() {

        Product product = createProduct(1L, "Headphones", "500.00", 1);

        OrderRequestDto request = createOrderRequest(1L, 5);

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        doThrow(new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Insufficient stock"
        )).when(productService).deductStock(1L, 5);

        assertThrows(
                ResponseStatusException.class,
                () -> orderService.placeOrder(request)
        );

        verify(orderRepository, never()).save(any(Order.class));
    }

    // --------------------------------------------------
    // GET ORDER TESTS
    // --------------------------------------------------

    @Test
    void shouldGetOrderByIdSuccessfully() {

        Order order = createOrder(1L, OrderStatus.PENDING);

        OrderResponseDto expectedResponse = new OrderResponseDto();

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(orderMapper.toOrderResponseDto(order))
                .thenReturn(expectedResponse);

        OrderResponseDto response = orderService.getOrderById(1L);

        assertNotNull(response);

        verify(orderRepository).findById(1L);
        verify(orderMapper).toOrderResponseDto(order);
    }

    @Test
    void shouldThrowExceptionWhenOrderDoesNotExist() {

        when(orderRepository.findById(999L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> orderService.getOrderById(999L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    // --------------------------------------------------
    // ORDER STATUS TESTS
    // --------------------------------------------------

    @Test
    void shouldConfirmPendingOrder() {

        Order order = createOrder(1L, OrderStatus.PENDING);

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(orderRepository.save(order)).thenReturn(order);

        when(orderMapper.toOrderResponseDto(order))
                .thenReturn(new OrderResponseDto());

        orderService.updateOrderStatus(1L, OrderStatus.CONFIRMED);

        assertEquals(OrderStatus.CONFIRMED, order.getOrderStatus());

        verify(productService, never())
                .restoreStock(anyLong(), anyInt());
    }

    @Test
    void shouldRejectInvalidStatusTransition() {

        Order order = createOrder(1L, OrderStatus.CONFIRMED);

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> orderService.updateOrderStatus(
                        1L, OrderStatus.PENDING
                )
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());

        verify(orderRepository, never()).save(any(Order.class));
        verify(productService, never())
                .restoreStock(anyLong(), anyInt());
    }

    @Test
    void shouldCancelOrderAndRestoreStock() {

        Order order = createOrder(1L, OrderStatus.PENDING);

        Product product = createProduct(10L, "Headphones", "500.00", 10);

        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setProduct(product);
        item.setQuantity(2);
        item.setUnitPrice(new BigDecimal("500.00"));
        item.setSubtotal(new BigDecimal("1000.00"));

        order.setOrderItems(List.of(item));

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(orderRepository.save(order)).thenReturn(order);

        when(orderMapper.toOrderResponseDto(order))
                .thenReturn(new OrderResponseDto());

        orderService.updateOrderStatus(1L, OrderStatus.CANCELLED);

        assertEquals(OrderStatus.CANCELLED, order.getOrderStatus());

        verify(productService).restoreStock(10L, 2);
        verify(orderRepository).save(order);
    }

    @Test
    void shouldNotCancelAlreadyCancelledOrder() {

        Order order = createOrder(1L, OrderStatus.CANCELLED);

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        assertThrows(
                ResponseStatusException.class,
                () -> orderService.updateOrderStatus(
                        1L, OrderStatus.CANCELLED
                )
        );

        verify(productService, never())
                .restoreStock(anyLong(), anyInt());

        verify(orderRepository, never()).save(any(Order.class));
    }

    // --------------------------------------------------
    // PAGINATION AND FILTER TESTS
    // --------------------------------------------------

    @Test
    void shouldGetAllOrdersWithPagination() {

        Pageable pageable = PageRequest.of(0, 10);

        Order order = createOrder(1L, OrderStatus.PENDING);

        Page<Order> orderPage = new PageImpl<>(
                List.of(order),
                pageable,
                1
        );

        when(orderRepository.findAll(pageable))
                .thenReturn(orderPage);

        when(orderMapper.toOrderResponseDto(order))
                .thenReturn(new OrderResponseDto());

        Page<OrderResponseDto> result =
                orderService.getAllOrders(pageable);

        assertEquals(1, result.getTotalElements());

        verify(orderRepository).findAll(pageable);
    }

    @Test
    void shouldFilterOrdersByStatus() {

        Pageable pageable = PageRequest.of(0, 10);

        Order order = createOrder(1L, OrderStatus.PENDING);

        Page<Order> orderPage = new PageImpl<>(
                List.of(order),
                pageable,
                1
        );

        when(orderRepository.findByOrderStatus(
                OrderStatus.PENDING, pageable
        )).thenReturn(orderPage);

        when(orderMapper.toOrderResponseDto(order))
                .thenReturn(new OrderResponseDto());

        Page<OrderResponseDto> result =
                orderService.getOrdersByStatus(
                        OrderStatus.PENDING, pageable
                );

        assertEquals(1, result.getTotalElements());

        verify(orderRepository).findByOrderStatus(
                OrderStatus.PENDING, pageable
        );
    }

    @Test
    void shouldFilterOrdersByCustomerEmail() {

        Pageable pageable = PageRequest.of(0, 10);

        Order order = createOrder(1L, OrderStatus.PENDING);

        Page<Order> orderPage = new PageImpl<>(
                List.of(order),
                pageable,
                1
        );

        when(orderRepository.findByCustomerEmailIgnoreCase(
                "john@example.com", pageable
        )).thenReturn(orderPage);

        when(orderMapper.toOrderResponseDto(order))
                .thenReturn(new OrderResponseDto());

        Page<OrderResponseDto> result =
                orderService.getOrdersByCustomerEmail(
                        "john@example.com", pageable
                );

        assertEquals(1, result.getTotalElements());

        verify(orderRepository).findByCustomerEmailIgnoreCase(
                "john@example.com", pageable
        );
    }

    @Test
    void shouldFilterOrdersByDateRange() {

        Pageable pageable = PageRequest.of(0, 10);

        LocalDateTime startDate =
                LocalDateTime.of(2026, 10, 1, 0, 0);

        LocalDateTime endDate =
                LocalDateTime.of(2026, 10, 31, 23, 59);

        Order order = createOrder(1L, OrderStatus.PENDING);

        Page<Order> orderPage = new PageImpl<>(
                List.of(order),
                pageable,
                1
        );

        when(orderRepository.findByCreatedAtBetween(
                startDate, endDate, pageable
        )).thenReturn(orderPage);

        when(orderMapper.toOrderResponseDto(order))
                .thenReturn(new OrderResponseDto());

        Page<OrderResponseDto> result =
                orderService.getOrdersByDateRange(
                        startDate, endDate, pageable
                );

        assertEquals(1, result.getTotalElements());

        verify(orderRepository).findByCreatedAtBetween(
                startDate, endDate, pageable
        );
    }

    @Test
    void shouldRejectInvalidDateRange() {

        LocalDateTime startDate =
                LocalDateTime.of(2026, 11, 1, 0, 0);

        LocalDateTime endDate =
                LocalDateTime.of(2026, 10, 1, 0, 0);

        Pageable pageable = PageRequest.of(0, 10);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> orderService.getOrdersByDateRange(
                        startDate, endDate, pageable
                )
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());

        verify(orderRepository, never())
                .findByCreatedAtBetween(
                        any(LocalDateTime.class),
                        any(LocalDateTime.class),
                        any(Pageable.class)
                );
    }

    // --------------------------------------------------
    // HELPER METHODS
    // --------------------------------------------------

    private Product createProduct(
            Long id,
            String name,
            String price,
            Integer stock
    ) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setPrice(new BigDecimal(price));
        product.setAvailableStock(stock);
        return product;
    }

    private Order createOrder(Long id, OrderStatus status) {

        Order order = new Order();

        order.setId(id);
        order.setCustomerName("John");
        order.setCustomerEmail("john@example.com");
        order.setTotalAmount(new BigDecimal("1000.00"));
        order.setOrderStatus(status);
        order.setCreatedAt(LocalDateTime.now());
        order.setOrderItems(List.of());

        return order;
    }

    private OrderRequestDto createOrderRequest(
            Long productId,
            Integer quantity
    ) {
        OrderRequestDto request = new OrderRequestDto();

        request.setCustomerName("John");
        request.setCustomerEmail("john@example.com");
        request.setItems(
                List.of(createOrderItemRequest(productId, quantity))
        );

        return request;
    }

    private OrderItemRequestDto createOrderItemRequest(
            Long productId,
            Integer quantity
    ) {
        OrderItemRequestDto item = new OrderItemRequestDto();

        item.setProductId(productId);
        item.setQuantity(quantity);

        return item;
    }

    @Test
    void shouldThrowExceptionWhenCancellingNonExistentOrder() {
        // Arrange
        Long orderId = 999L;

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> orderService.updateOrderStatus(orderId, OrderStatus.CANCELLED)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verify(productService, never()).restoreStock(anyLong(), anyInt());
    }
}
