package com.srijon.orderflow.repository;

import com.srijon.orderflow.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product , Long> {

    Page<Product> findByNameContainingIgnoreCase(String name , Pageable pageable);

    boolean existsByCategoryId(Long categoryId);

    Page<Product> findByCategoryId(Long categoryID , Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdWithLock(@Param("id") Long id);
}
