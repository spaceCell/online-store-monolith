package com.example.store.catalog.domain;

import com.example.store.catalog.exception.InsufficientStockException;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
public class Product {

    @Id
    @UuidGenerator
    private UUID id;

    private String name;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;

    public Product(String name, String description, BigDecimal price, Integer stockQuantity) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
    }

    public void reserveStock(Integer quantity) {
        if (stockQuantity < quantity) {
            throw new InsufficientStockException(
                    String.format("Insufficient stock. Available: %d, Requested: %d",
                            stockQuantity, quantity)
            );
        }
        stockQuantity -= quantity;
    }

    public void releaseStock(Integer quantity) {
        stockQuantity += quantity;
    }
}
