package com.srijon.orderflow.mapper;

import com.srijon.orderflow.dto.ProductRequestDto;
import com.srijon.orderflow.dto.ProductResponseDto;
import com.srijon.orderflow.entity.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    //Converting the productRequestDto to product
    public Product toEntity(ProductRequestDto productRequestDto){

        Product product = new Product();

        product.setName(productRequestDto.getName());
        product.setDescription(productRequestDto.getDescription());
        product.setPrice(productRequestDto.getPrice());
        product.setAvailableStock(productRequestDto.getAvailableStock());

        return product;
    }

    //Converting a product to productResponseDto
    public ProductResponseDto toResponseDto(Product product){

        ProductResponseDto productResponseDto = new ProductResponseDto();

        productResponseDto.setId(product.getId());
        productResponseDto.setName(product.getName());
        productResponseDto.setDescription(product.getDescription());
        productResponseDto.setPrice(product.getPrice());
        productResponseDto.setAvailableStock(product.getAvailableStock());
        productResponseDto.setCreated(product.getCreated());
        productResponseDto.setUpdated(product.getUpdated());

        return productResponseDto;
    }
}
