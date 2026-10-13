package com.srijon.orderflow.service.impl;

import com.srijon.orderflow.entity.Product;
import com.srijon.orderflow.repository.ProductRepository;
import com.srijon.orderflow.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public Product getProductById(Long id) {
        return productRepository
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND , "Product not found"));
    }

    @Override
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    @Override
    public Product updateProduct(Long id, Product product) {

        Product update = productRepository
                .findById(id)
                .orElseThrow( () -> new ResponseStatusException(HttpStatus.NOT_FOUND , "Product not found"));

        //Updating the product if we find it
        update.setName(product.getName());
        update.setPrice(product.getPrice());
        update.setAvailableStock(product.getAvailableStock());
        update.setDescription(product.getDescription());

        return productRepository.save(update);
    }

    @Override
    public void deleteProduct(Long id) {

        Product deleteProduct = productRepository
                .findById(id)
                .orElseThrow( () -> new ResponseStatusException( HttpStatus.NOT_FOUND , "Product not found"));

        productRepository.delete(deleteProduct);
    }
}
