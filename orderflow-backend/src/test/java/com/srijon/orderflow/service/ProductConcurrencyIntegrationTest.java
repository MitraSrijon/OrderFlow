package com.srijon.orderflow.service;

import com.srijon.orderflow.OrderflowBackendApplication;
import com.srijon.orderflow.entity.Category;
import com.srijon.orderflow.entity.Product;
import com.srijon.orderflow.repository.CategoryRepository;
import com.srijon.orderflow.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = OrderflowBackendApplication.class)
@ActiveProfiles("test")
public class ProductConcurrencyIntegrationTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    //Creating a test category
    private Category createTestCategory() {
        Category category = new Category();
        category.setName("Concurrency Test Category");
        category.setDescription("Category for concurrency testing");

        return categoryRepository.save(category);
    }

    //Creating a test product
    private Product createTestProduct() {
        Product product = new Product();
        product.setName("Concurrency Test Product");
        product.setDescription("Testing stock locking");
        product.setPrice(new BigDecimal("100.00"));
        product.setAvailableStock(5);
        product.setCategory(createTestCategory());

        return productRepository.save(product);
    }

    @BeforeEach
    void cleanDatabases(){
        productRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    //Checking if this is connecting to orderflow_test database
    @Test
    void testDatabaseConnection() {
        Category category = createTestCategory();

        assertNotNull(category.getId());
    }

    //Actual test, when 2 requests comes simultaneously
    //executorService = Runs task using separate threads
    //CountDownLatch = Coordinates when both the request starts
    //Future = collects result of each request
    @Test
    void shouldPreventOversellingWhenTwoRequestsArriveTogether() throws Exception {

        Product product = createTestProduct();

        ExecutorService executor = Executors.newFixedThreadPool(2);

        CountDownLatch startSignal = new CountDownLatch(1);

        Future<Boolean> requestA = executor.submit(() -> {
            startSignal.await();

            try {
                productService.deductStock(product.getId(), 4);
                return true;
            } catch (ResponseStatusException ex) {
                return false;
            }
        });

        Future<Boolean> requestB = executor.submit(() -> {
            startSignal.await();

            try {
                productService.deductStock(product.getId(), 3);
                return true;
            } catch (ResponseStatusException ex) {
                return false;
            }
        });

        startSignal.countDown();

        boolean resultA = requestA.get(10, TimeUnit.SECONDS);
        boolean resultB = requestB.get(10, TimeUnit.SECONDS);

        executor.shutdownNow();

        assertTrue(resultA ^ resultB);

        Product updatedProduct = productRepository.findById(product.getId())
                .orElseThrow();

        assertTrue(
                updatedProduct.getAvailableStock() == 1 ||
                        updatedProduct.getAvailableStock() == 2
        );
    }

    //When 2 requests come simultaneously and we have enough stock to satisfy both requests
    @Test
    void shouldAllowBothRequestsWhenStockIsSufficient() throws Exception {

        Product product = createTestProduct();

        ExecutorService executor = Executors.newFixedThreadPool(2);

        CountDownLatch startSignal = new CountDownLatch(1);

        Future<Boolean> requestA = executor.submit(() -> {
            startSignal.await();

            try {
                productService.deductStock(product.getId(), 3);
                return true;
            } catch (ResponseStatusException ex) {
                return false;
            }
        });

        Future<Boolean> requestB = executor.submit(() -> {
            startSignal.await();

            try {
                productService.deductStock(product.getId(), 4);
                return true;
            } catch (ResponseStatusException ex) {
                return false;
            }
        });

        startSignal.countDown();

        boolean resultA = requestA.get(10, TimeUnit.SECONDS);
        boolean resultB = requestB.get(10, TimeUnit.SECONDS);

        executor.shutdownNow();

        assertTrue(resultA);
        assertTrue(resultB);

        Product updatedProduct = productRepository.findById(product.getId())
                .orElseThrow();

        assertEquals(3, updatedProduct.getAvailableStock());
    }

    //Checks when both the condition fails
    @Test
    void shouldRejectBothRequestsWhenStockIsInsufficient() throws Exception {

        Product product = createTestProduct();

        ExecutorService executor = Executors.newFixedThreadPool(2);

        CountDownLatch startSignal = new CountDownLatch(1);

        Future<Boolean> requestA = executor.submit(() -> {
            startSignal.await();

            try {
                productService.deductStock(product.getId(), 6);
                return true;
            } catch (ResponseStatusException ex) {
                return false;
            }
        });

        Future<Boolean> requestB = executor.submit(() -> {
            startSignal.await();

            try {
                productService.deductStock(product.getId(), 7);
                return true;
            } catch (ResponseStatusException ex) {
                return false;
            }
        });

        startSignal.countDown();

        boolean resultA = requestA.get(10, TimeUnit.SECONDS);
        boolean resultB = requestB.get(10, TimeUnit.SECONDS);

        executor.shutdownNow();

        assertFalse(resultA);
        assertFalse(resultB);

        Product updatedProduct = productRepository.findById(product.getId())
                .orElseThrow();

        assertEquals(5, updatedProduct.getAvailableStock());
    }

    //When we have more than 2 users requesting same but we have enough stock to satisfy the call
    @Test
    void shouldHandleMultipleConcurrentStockDeductions() throws Exception {

        Product product = createTestProduct();

        int noOfRequest = 20;

        ExecutorService executor = Executors.newFixedThreadPool(noOfRequest);

        CountDownLatch startSignal = new CountDownLatch(1);

        List<Future<Boolean>> results = new ArrayList<>();

        for (int i = 0; i < noOfRequest; i++) {

            Future<Boolean> request = executor.submit(() -> {
                startSignal.await();

                try {
                    productService.deductStock(product.getId(), 5);
                    return true;
                } catch (ResponseStatusException ex) {
                    return false;
                }
            });

            results.add(request);
        }

        startSignal.countDown();

        int successfulRequests = 0;

        for (Future<Boolean> result : results) {
            if (result.get(30, TimeUnit.SECONDS)) {
                successfulRequests++;
            }
        }

        executor.shutdownNow();

        assertEquals(noOfRequest, successfulRequests);

        Product updatedProduct = productRepository.findById(product.getId())
                .orElseThrow();

        assertEquals(0, updatedProduct.getAvailableStock());
    }

    //When 20 users request for 5 stock each and we have only 50, so 10 user passes and 10 fails
    @Test
    void shouldRejectExcessConcurrentStockRequests() throws Exception {

        Product product = createTestProduct();

        int noOfRequest = 20;

        ExecutorService executor = Executors.newFixedThreadPool(noOfRequest);

        CountDownLatch startSignal = new CountDownLatch(1);

        List<Future<Boolean>> results = new ArrayList<>();

        for (int i = 0; i < noOfRequest; i++) {

            Future<Boolean> request = executor.submit(() -> {
                startSignal.await();

                try {
                    productService.deductStock(product.getId(), 5);
                    return true;
                } catch (ResponseStatusException ex) {
                    return false;
                }
            });

            results.add(request);
        }

        startSignal.countDown();

        int successfulRequests = 0;

        for (Future<Boolean> result : results) {
            if (result.get(30, TimeUnit.SECONDS)) {
                successfulRequests++;
            }
        }

        executor.shutdownNow();

        int failedRequests = noOfRequest - successfulRequests;

        assertEquals(10, successfulRequests);
        assertEquals(10,failedRequests);

        Product updatedProduct = productRepository.findById(product.getId())
                .orElseThrow();

        assertEquals(0, updatedProduct.getAvailableStock());
    }

}
