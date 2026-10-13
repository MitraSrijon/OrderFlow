package com.srijon.orderflow.service;

import com.srijon.orderflow.entity.Product;

import java.util.List;

//Basic structure of Product Service
public interface ProductService {

    //Retrieve all the products
    public List<Product> getAllProducts();

    //Get Product by id
    public  Product getProductById(Long id);

    //Save a product
    public Product saveProduct(Product product);

    //update a product
    public Product updateProduct(Long id , Product product);

    //Delete a product
    public void deleteProduct(Long id);
}
