package com.example.OrderFlow.OrderService.Model;

import com.example.OrderFlow.Common.Entity.AuditableBase;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "carts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cart extends AuditableBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long userId;

    @Builder.Default
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();

    @Column(nullable = false)
    private BigDecimal totalAmount;


    public void recalculateTotal() {
        this.totalAmount = items.stream()
                .map(CartItem::getSubTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void addItem(CartItem item){
        this.items.add(item);
        item.setCart(this);
    }

    public void removeItem(CartItem item){
        this.items.remove(item);
        item.setCart(null);
    }

    public void clearItems() {
        this.items.forEach(item -> item.setCart(null));
        this.items.clear();
    }
}
