package com.example.store.catalog.service;

import com.example.store.catalog.domain.Product;

import java.util.UUID;

public interface CatalogService {

    Product reserveProduct(UUID productId, Integer quantity);

    void releaseProduct(UUID productId, Integer quantity);
}
