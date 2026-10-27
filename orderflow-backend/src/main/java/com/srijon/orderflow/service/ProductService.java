package com.srijon.orderflow.service;

import com.srijon.orderflow.dto.ProductDto.ProductRequestDto;
import com.srijon.orderflow.dto.ProductDto.ProductResponseDto;
import com.srijon.orderflow.dto.ProductDto.StockUpdateRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

//Basic structure of Product Service
public interface ProductService {

    //Retrieve all the products
    public Page<ProductResponseDto> getAllProducts(Pageable pageable);

    //Get Product by id
    public  ProductResponseDto getProductById(Long id);

    //Save a product
    public ProductResponseDto saveProduct(ProductRequestDto productRequestDto);

    //update a product
    public ProductResponseDto updateProduct(Long id , ProductRequestDto productRequestDto);

    //Delete a product
    public void deleteProduct(Long id);

    //Search a product
    Page<ProductResponseDto> searchProducts(
            String name, Pageable pageable);

    //Search products by category
    Page<ProductResponseDto> getProductsByCategory(Long categoryId ,  Pageable pageable);

    //Update the stock quantity
    ProductResponseDto updateStock(Long id , StockUpdateRequestDto stockUpdateRequestDto);

    //Reducing the stock when an order is placed
    void deductStock(Long productId, Integer quantity);
}
