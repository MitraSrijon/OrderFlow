package com.srijon.orderflow.service.impl;

import com.srijon.orderflow.dto.ProductDto.ProductRequestDto;
import com.srijon.orderflow.dto.ProductDto.ProductResponseDto;
import com.srijon.orderflow.entity.Category;
import com.srijon.orderflow.entity.Product;
import com.srijon.orderflow.mapper.ProductMapper;
import com.srijon.orderflow.repository.CategoryRepository;
import com.srijon.orderflow.repository.ProductRepository;
import com.srijon.orderflow.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper mapper;
    private final CategoryRepository categoryRepository;

    //helper method
    private Category findByCategoryId(Long categoryId){

        return categoryRepository.findById(categoryId)
                .orElseThrow( () -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND , "Category not found"
                ));
    }

    @Override
    public Page<ProductResponseDto> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable)
                .map(mapper::toResponseDto);
    }

    @Override
    public ProductResponseDto getProductById(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND , "Product not found"));;

        return mapper.toResponseDto(product);

    }

    @Override
    public ProductResponseDto saveProduct(ProductRequestDto productRequestDto) {

        Product product = mapper.toEntity(productRequestDto);

        Category category = findByCategoryId(productRequestDto.getCategoryId());
        product.setCategory(category);

        return mapper.toResponseDto(productRepository.save(product));
    }

    @Override
    public ProductResponseDto updateProduct(Long id, ProductRequestDto productRequestDto) {

        Product update = productRepository
                .findById(id)
                .orElseThrow( () -> new ResponseStatusException(HttpStatus.NOT_FOUND , "Product not found"));

        //Updating the product if we find it
        update.setName(productRequestDto.getName());
        update.setPrice(productRequestDto.getPrice());
        update.setAvailableStock(productRequestDto.getAvailableStock());
        update.setDescription(productRequestDto.getDescription());
        update.setCategory(findByCategoryId(productRequestDto.getCategoryId()));

        return mapper.toResponseDto(productRepository.save(update));
    }

    @Override
    public void deleteProduct(Long id) {

        Product deleteProduct = productRepository
                .findById(id)
                .orElseThrow( () -> new ResponseStatusException( HttpStatus.NOT_FOUND , "Product not found"));

        productRepository.delete(deleteProduct);
    }

    @Override
    public Page<ProductResponseDto> searchProducts(String name, Pageable pageable) {

        return productRepository.findByNameContainingIgnoreCase(name , pageable)
                .map(mapper::toResponseDto);
    }
}
