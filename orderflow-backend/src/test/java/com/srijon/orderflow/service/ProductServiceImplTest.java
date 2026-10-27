
package com.srijon.orderflow.service;

import com.srijon.orderflow.mapper.ProductMapper;
import com.srijon.orderflow.entity.Product;
import com.srijon.orderflow.repository.CategoryRepository;
import com.srijon.orderflow.repository.ProductRepository;
import com.srijon.orderflow.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductMapper mapper;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setName("Keyboard");
        product.setAvailableStock(10);
    }

    @Test
    void deductStock_shouldReduceAvailableStock() {
        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        productService.deductStock(1L, 3);

        assertEquals(7, product.getAvailableStock());
        verify(productRepository).save(product);
    }

    @Test
    void deductStock_shouldAllowStockToBecomeZero() {
        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        productService.deductStock(1L, 10);

        assertEquals(0, product.getAvailableStock());
        verify(productRepository).save(product);
    }

    @Test
    void deductStock_shouldRejectInsufficientStock() {
        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        assertThrows(ResponseStatusException.class,
                () -> productService.deductStock(1L, 11));

        assertEquals(10, product.getAvailableStock());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void deductStock_shouldRejectZeroOrNegativeQuantity() {
        assertThrows(ResponseStatusException.class,
                () -> productService.deductStock(1L, 0));

        assertThrows(ResponseStatusException.class,
                () -> productService.deductStock(1L, -1));

        verify(productRepository, never()).findById(anyLong());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void deductStock_shouldRejectMissingProduct() {
        when(productRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> productService.deductStock(99L, 1));

        verify(productRepository, never()).save(any(Product.class));
    }
}
