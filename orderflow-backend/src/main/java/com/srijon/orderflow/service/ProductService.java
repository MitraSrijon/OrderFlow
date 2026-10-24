package com.srijon.orderflow.service;

import com.srijon.orderflow.dto.ProductRequestDto;
import com.srijon.orderflow.dto.ProductResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.util.List;

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
}
