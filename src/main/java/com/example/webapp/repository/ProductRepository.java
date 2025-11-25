package com.example.webapp.repository;

import com.example.webapp.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Method 1: Using Spring Data JPA query derivation
    List<Product> findByStockCountLessThan(Integer stockCount);

    // Method 2: Using Spring Data JPA query derivation with different name
    List<Product> findByStockCountLessThanEqual(Integer stockCount);

    // Method 3: Using @Query annotation for custom JPQL query
    @Query("SELECT p FROM Product p WHERE p.stockCount < :stockThreshold")
    List<Product> findLowStockProducts(@Param("stockThreshold") Integer stockThreshold);

    // Method 4: Using @Query annotation with native SQL
    @Query(value = "SELECT * FROM products WHERE stock_count < :stockThreshold", nativeQuery = true)
    List<Product> findLowStockProductsNative(@Param("stockThreshold") Integer stockThreshold);

    // Additional useful methods for stock management:
    List<Product> findByCategory(String category);

    List<Product> findByStockCountGreaterThan(Integer stockCount);

    List<Product> findByStockCountBetween(Integer minStock, Integer maxStock);

    // Find products that are out of stock
    List<Product> findByStockCountEquals(Integer stockCount);

    // Find products with stock count not null and less than threshold
    @Query("SELECT p FROM Product p WHERE p.stockCount IS NOT NULL AND p.stockCount < :stockThreshold")
    List<Product> findLowStockProductsNotNull(@Param("stockThreshold") Integer stockThreshold);

    @Query("SELECT p FROM Product p ORDER BY p.createdAt DESC LIMIT :limit")
    List<Product> findLatestProducts(@Param("limit") int limit);
}