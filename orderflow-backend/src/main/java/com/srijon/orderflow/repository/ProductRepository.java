package com.srijon.orderflow.repository;

import com.srijon.orderflow.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product , Long> {

    Page<Product> findByNameContainingIgnoreCase(String name , Pageable pageable);

    boolean existsByCategoryId(Long categoryId);

    Page<Product> findByCategoryId(Long categoryID , Pageable pageable);
}
