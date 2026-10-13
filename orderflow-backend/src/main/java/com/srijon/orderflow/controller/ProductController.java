package com.srijon.orderflow.controller;

import com.srijon.orderflow.entity.Product;
import com.srijon.orderflow.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    //Getting all the products
    @GetMapping
    public List<Product> getAllProducts(){

        return productService.getAllProducts();
    }

    //Get a specific product by giving id
    @GetMapping("/{id}")
    public Product getProductById(@PathVariable Long id){

        return productService.getProductById(id);
    }

    //Creating a product
    @PostMapping
    public Product createProduct(@RequestBody Product product){

        return productService.saveProduct(product);
    }

    //Updating a product
    @PutMapping("/{id}")
    public Product updateProduct(
            @PathVariable Long id,
            @RequestBody Product product
    ){

        return productService.updateProduct(id , product);
    }

    //Deleting a product
    @DeleteMapping("/{id}")
    public  void deleteProduct(@PathVariable Long id){

        productService.deleteProduct(id);
    }


}
