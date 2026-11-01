
package com.srijon.orderflow;

import com.srijon.orderflow.dto.OrderDto.OrderResponseDto;
import com.srijon.orderflow.dto.OrderItemDto.OrderItemRequestDto;
import com.srijon.orderflow.dto.OrderDto.OrderRequestDto;
import com.srijon.orderflow.entity.Category;
import com.srijon.orderflow.entity.Product;
import com.srijon.orderflow.enums.OrderStatus;
import com.srijon.orderflow.repository.CategoryRepository;
import com.srijon.orderflow.repository.OrderItemRepository;
import com.srijon.orderflow.repository.OrderRepository;
import com.srijon.orderflow.repository.ProductRepository;
import com.srijon.orderflow.service.OrderService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class OrderTransactionIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Test
    void shouldRollbackEntireOrderWhenOneProductDoesNotExist() {

        // Arrange: create a category
        Category category = new Category();
        category.setName("Test-" + UUID.randomUUID());
        category.setDescription("Integration test category");

        Category savedCategory = categoryRepository.save(category);

        // Arrange: create a product with stock = 10
        Product product = new Product();
        product.setName("Integration Test Product");
        product.setDescription("Rollback test");
        product.setPrice(new BigDecimal("500.00"));
        product.setAvailableStock(10);
        product.setCategory(savedCategory);

        Product savedProduct = productRepository.save(product);

        try {
            // Arrange: create two order items

            // First item: valid product
            OrderItemRequestDto validItem = new OrderItemRequestDto();
            validItem.setProductId(savedProduct.getId());
            validItem.setQuantity(2);

            // Second item: nonexistent product
            OrderItemRequestDto invalidItem = new OrderItemRequestDto();
            invalidItem.setProductId(Long.MAX_VALUE);
            invalidItem.setQuantity(1);

            OrderRequestDto request = new OrderRequestDto();
            request.setCustomerName("Integration Test");
            request.setCustomerEmail("test@example.com");
            request.setItems(List.of(validItem, invalidItem));

            // Record database counts before order placement
            long ordersBefore = orderRepository.count();
            long orderItemsBefore = orderItemRepository.count();

            // Act + Assert: order placement must fail
            assertThrows(
                    ResponseStatusException.class,
                    () -> orderService.placeOrder(request)
            );

            // Assert: stock deduction must have rolled back
            Product updatedProduct = productRepository
                    .findById(savedProduct.getId())
                    .orElseThrow();

            assertEquals(10, updatedProduct.getAvailableStock());

            // Assert: no order should have been created
            assertEquals(ordersBefore, orderRepository.count());

            // Assert: no order items should have been created
            assertEquals(orderItemsBefore, orderItemRepository.count());

        } finally {
            // Clean up test records
            productRepository.deleteById(savedProduct.getId());
            categoryRepository.deleteById(savedCategory.getId());
        }
    }

    @Test
    void shouldPlaceOrderAndUpdateStockSuccessfully() {

        // Arrange: create category
        Category category = new Category();
        category.setName("Success-" + UUID.randomUUID());
        category.setDescription("Successful order test");

        Category savedCategory = categoryRepository.save(category);

        // Arrange: create product with stock = 10
        Product product = new Product();
        product.setName("Successful Test Product");
        product.setPrice(new BigDecimal("500.00"));
        product.setAvailableStock(10);
        product.setCategory(savedCategory);

        Product savedProduct = productRepository.save(product);

        Long createdOrderId = null;

        try {
            // Create order item: quantity = 2
            OrderItemRequestDto item = new OrderItemRequestDto();
            item.setProductId(savedProduct.getId());
            item.setQuantity(2);

            // Create order request
            OrderRequestDto request = new OrderRequestDto();
            request.setCustomerName("Integration Test");
            request.setCustomerEmail("success@example.com");
            request.setItems(List.of(item));

            // Act: place order
            var response = orderService.placeOrder(request);
            createdOrderId = response.getId();

            // Assert: order was created
            assertNotNull(response.getId());
            assertEquals(
                    new BigDecimal("1000.00"),
                    response.getTotalAmount()
            );

            // Assert: order item was saved
            assertEquals(1, response.getItems().size());
            assertEquals(2, response.getItems().get(0).getQuantity());

            // Assert: stock decreased from 10 to 8
            Product updatedProduct = productRepository
                    .findById(savedProduct.getId())
                    .orElseThrow();

            assertEquals(8, updatedProduct.getAvailableStock());

            // Assert: order exists in database
            assertTrue(orderRepository.existsById(response.getId()));

        } finally {
            // Delete the order first.
            // CascadeType.ALL deletes its order items.
            if (createdOrderId != null) {
                orderRepository.deleteById(createdOrderId);
            }

            // Now the product can safely be deleted.
            productRepository.deleteById(savedProduct.getId());

            categoryRepository.deleteById(savedCategory.getId());
        }

    }

    @Test
    void shouldRestoreStockWhenOrderIsCancelled() {

        Category category = new Category();
        category.setName("Cancellation-" + UUID.randomUUID());
        category.setDescription("Cancellation integration test");

        Category savedCategory = categoryRepository.save(category);

        Product product = new Product();
        product.setName("Cancellation Test Product");
        product.setPrice(new BigDecimal("500.00"));
        product.setAvailableStock(10);
        product.setCategory(savedCategory);

        Product savedProduct = productRepository.save(product);

        Long createdOrderId = null;

        try {
            // Create order request
            OrderItemRequestDto item = new OrderItemRequestDto();
            item.setProductId(savedProduct.getId());
            item.setQuantity(2);

            OrderRequestDto request = new OrderRequestDto();
            request.setCustomerName("Cancellation Test");
            request.setCustomerEmail("cancel@example.com");
            request.setItems(List.of(item));

            // Place order
            var response = orderService.placeOrder(request);
            createdOrderId = response.getId();

            // Verify stock deduction: 10 -> 8
            Product afterPlacement = productRepository
                    .findById(savedProduct.getId())
                    .orElseThrow();

            assertEquals(8, afterPlacement.getAvailableStock());

            // Cancel order
            orderService.updateOrderStatus(
                    createdOrderId,
                    OrderStatus.CANCELLED
            );

            // Verify order status
            OrderResponseDto cancelledOrder =
                    orderService.getOrderById(createdOrderId);

            assertEquals(OrderStatus.CANCELLED, cancelledOrder.getOrderStatus());

            // Verify stock restoration: 8 -> 10
            Product afterCancellation = productRepository
                    .findById(savedProduct.getId())
                    .orElseThrow();

            assertEquals(10, afterCancellation.getAvailableStock());

        } finally {
            if (createdOrderId != null) {
                orderRepository.deleteById(createdOrderId);
            }

            productRepository.deleteById(savedProduct.getId());
            categoryRepository.deleteById(savedCategory.getId());
        }
    }


    @Test
    void shouldNotRestoreStockWhenCancelledOrderIsCancelledAgain() {

        Category category = new Category();
        category.setName("DoubleCancel-" + UUID.randomUUID());
        category.setDescription("Double cancellation test");
        Category savedCategory = categoryRepository.save(category);

        Product product = new Product();
        product.setName("Double Cancellation Test Product");
        product.setPrice(new BigDecimal("500.00"));
        product.setAvailableStock(10);
        product.setCategory(savedCategory);
        Product savedProduct = productRepository.save(product);

        Long createdOrderId = null;

        try {
            OrderItemRequestDto item = new OrderItemRequestDto();
            item.setProductId(savedProduct.getId());
            item.setQuantity(2);

            OrderRequestDto request = new OrderRequestDto();
            request.setCustomerName("Double Cancellation Test");
            request.setCustomerEmail("doublecancel@example.com");
            request.setItems(List.of(item));

            var response = orderService.placeOrder(request);
            createdOrderId = response.getId();

            // First cancellation should restore stock.
            orderService.updateOrderStatus(
                    createdOrderId, OrderStatus.CANCELLED);

            Product afterFirstCancellation = productRepository
                    .findById(savedProduct.getId())
                    .orElseThrow();

            assertEquals(10, afterFirstCancellation.getAvailableStock());

            Long orderIdToCancel = createdOrderId;

            // Second cancellation should be rejected.
            assertThrows(ResponseStatusException.class,
                    () -> orderService.updateOrderStatus(
                            orderIdToCancel, OrderStatus.CANCELLED));

            // Stock must remain unchanged.
            Product afterSecondCancellation = productRepository
                    .findById(savedProduct.getId())
                    .orElseThrow();

            assertEquals(10, afterSecondCancellation.getAvailableStock());

        } finally {
            if (createdOrderId != null) {
                orderRepository.deleteById(createdOrderId);
            }
            productRepository.deleteById(savedProduct.getId());
            categoryRepository.deleteById(savedCategory.getId());
        }
    }


    @Test
    void shouldNotCreateOrderWhenStockIsInsufficient() {

        Category category = new Category();
        category.setName("InsufficientStock-" + UUID.randomUUID());
        category.setDescription("Insufficient stock test");
        Category savedCategory = categoryRepository.save(category);

        Product product = new Product();
        product.setName("Insufficient Stock Test Product");
        product.setPrice(new BigDecimal("500.00"));
        product.setAvailableStock(5);
        product.setCategory(savedCategory);
        Product savedProduct = productRepository.save(product);

        try {
            OrderItemRequestDto item = new OrderItemRequestDto();
            item.setProductId(savedProduct.getId());
            item.setQuantity(8);

            OrderRequestDto request = new OrderRequestDto();
            request.setCustomerName("Stock Test");
            request.setCustomerEmail("stocktest@example.com");
            request.setItems(List.of(item));

            long ordersBefore = orderRepository.count();
            long orderItemsBefore = orderItemRepository.count();

            assertThrows(ResponseStatusException.class,
                    () -> orderService.placeOrder(request));

            Product updatedProduct = productRepository
                    .findById(savedProduct.getId())
                    .orElseThrow();

            assertEquals(5, updatedProduct.getAvailableStock());
            assertEquals(ordersBefore, orderRepository.count());
            assertEquals(orderItemsBefore, orderItemRepository.count());

        } finally {
            productRepository.deleteById(savedProduct.getId());
            categoryRepository.deleteById(savedCategory.getId());
        }
    }


    @Test
    void shouldRejectOrderWhenDuplicateQuantitiesOverflowInteger() {

        Category category = new Category();
        category.setName("Overflow-" + UUID.randomUUID());
        category.setDescription("Quantity overflow test");
        Category savedCategory = categoryRepository.save(category);

        Product product = new Product();
        product.setName("Overflow Test Product");
        product.setPrice(new BigDecimal("100.00"));
        product.setAvailableStock(10);
        product.setCategory(savedCategory);
        Product savedProduct = productRepository.save(product);

        try {
            OrderItemRequestDto firstItem = new OrderItemRequestDto();
            firstItem.setProductId(savedProduct.getId());
            firstItem.setQuantity(Integer.MAX_VALUE);

            OrderItemRequestDto secondItem = new OrderItemRequestDto();
            secondItem.setProductId(savedProduct.getId());
            secondItem.setQuantity(1);

            OrderRequestDto request = new OrderRequestDto();
            request.setCustomerName("Overflow Test");
            request.setCustomerEmail("overflow@example.com");
            request.setItems(List.of(firstItem, secondItem));

            long ordersBefore = orderRepository.count();
            long itemsBefore = orderItemRepository.count();

            assertThrows(ResponseStatusException.class,
                    () -> orderService.placeOrder(request));

            Product updatedProduct = productRepository
                    .findById(savedProduct.getId())
                    .orElseThrow();

            assertEquals(10, updatedProduct.getAvailableStock());
            assertEquals(ordersBefore, orderRepository.count());
            assertEquals(itemsBefore, orderItemRepository.count());

        } finally {
            productRepository.deleteById(savedProduct.getId());
            categoryRepository.deleteById(savedCategory.getId());
        }
    }


}
