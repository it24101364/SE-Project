package com.example.webapp.repository;

import com.example.webapp.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Get featured products (most recently added)
     * Limits to specified amount
     */
    @Query(value = "SELECT * FROM products ORDER BY created_at DESC LIMIT :limit",
            nativeQuery = true)
    List<Product> findFeaturedProducts(@Param("limit") int limit);

    /**
     * Alternative: Get top selling products (based on sales count)
     * Requires a sales_count or similar field in Product entity
     */
    @Query(value = "SELECT * FROM products ORDER BY sales_count DESC LIMIT :limit",
            nativeQuery = true)
    List<Product> findTopSellingProducts(@Param("limit") int limit);

    /**
     * Get random featured products for variety
     */
    @Query(value = "SELECT * FROM products ORDER BY RAND() LIMIT :limit",
            nativeQuery = true)
    List<Product> findRandomFeaturedProducts(@Param("limit") int limit);

    /**
     * Get products by category
     */
    List<Product> findByCategory(String category);

    /**
     * Search products by name or description
     */
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Product> searchProducts(@Param("searchTerm") String searchTerm);

    /**
     * Get in-stock products
     */
    @Query("SELECT p FROM Product p WHERE p.stockCount > 0 ORDER BY p.createdAt DESC")
    List<Product> findInStockProducts();

    /**
     * Get products with stock greater than specified amount
     */
    @Query("SELECT p FROM Product p WHERE p.stockCount > :minStock")
    List<Product> findProductsWithMinStock(@Param("minStock") int minStock);
}