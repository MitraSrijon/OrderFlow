package com.srijon.orderflow.repository;


import com.srijon.orderflow.entity.OrderEntities.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.ResponseBody;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
