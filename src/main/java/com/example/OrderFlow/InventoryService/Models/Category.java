package com.example.OrderFlow.InventoryService.Models;

import com.example.OrderFlow.Common.Entity.AuditableBase;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category extends AuditableBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, name = "cat_name")
    private String name;

    private String description;
}
