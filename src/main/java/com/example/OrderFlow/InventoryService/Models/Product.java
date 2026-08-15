package com.example.OrderFlow.InventoryService.Models;

import com.example.OrderFlow.Common.Entity.AuditableBase;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Check;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "products")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product extends AuditableBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private String sku;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false, check = @CheckConstraint(constraint = "price >= 0"))
    private BigDecimal price;

    @Column(nullable = false, name = "stock_qty", check = @CheckConstraint(constraint = "stock_qty >= 0"))
    private Integer stockQuantity;

    @Column(nullable = false, name = "low_stock_qty", check = @CheckConstraint(constraint = "low_stock_qty >=0"))
    private Integer lowStockThreshold;

    @ManyToOne
    @JoinColumn(name = "category_id",nullable = false)
    private Category category;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

}
