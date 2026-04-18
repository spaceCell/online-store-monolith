package com.example.store.catalog.service.impl;

import com.example.store.catalog.domain.Product;
import com.example.store.catalog.repository.ProductRepository;
import com.example.store.catalog.service.CatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CatalogServiceImpl implements CatalogService {

    private final ProductRepository productRepository;

    @Override
    public Product reserveProduct(UUID productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));

        product.reserveStock(quantity);
        return productRepository.save(product);
    }

    @Override
    public void releaseProduct(UUID productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));

        product.releaseStock(quantity);
        productRepository.save(product);
    }
}
