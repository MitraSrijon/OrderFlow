package com.srijon.orderflow.controller;

import com.srijon.orderflow.dto.ProductRequestDto;
import com.srijon.orderflow.dto.ProductResponseDto;
import com.srijon.orderflow.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    //Getting all the products
    @GetMapping
    public ResponseEntity<List<ProductResponseDto>> getAllProducts(){

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(productService.getAllProducts());
    }

    //Get a specific product by giving id
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDto> getProductById(@PathVariable Long id){

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(productService.getProductById(id));
    }

    //Creating a product
    @PostMapping
    public ResponseEntity<ProductResponseDto> createProduct(@Valid @RequestBody ProductRequestDto productRequestDto){

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(productService.saveProduct(productRequestDto));
    }

    //Updating a product
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponseDto> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequestDto productRequestDto
    ){

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(productService.updateProduct(id , productRequestDto));
    }

    //Deleting a product
    @DeleteMapping("/{id}")
    public  ResponseEntity<Void> deleteProduct(@PathVariable Long id){

        productService.deleteProduct(id);

        return ResponseEntity.noContent().build();
    }


}
