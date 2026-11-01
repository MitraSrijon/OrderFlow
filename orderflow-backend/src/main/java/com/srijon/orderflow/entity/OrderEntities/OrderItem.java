package com.srijon.orderflow.entity.OrderEntities;

import com.srijon.orderflow.entity.Product;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_Id" , nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_Id" ,  nullable = false)
    private Product product;

    private Integer quantity;

    private BigDecimal unitPrice;

    private BigDecimal subtotal;

}


