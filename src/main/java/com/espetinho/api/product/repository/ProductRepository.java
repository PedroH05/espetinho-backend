package com.espetinho.api.product.repository;

import com.espetinho.api.product.entity.Product;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    @EntityGraph(attributePaths = {"category"})
    @Query("""
            SELECT product
            FROM Product product
            JOIN product.category category
            WHERE product.active = true
              AND category.active = true
              AND (:available IS NULL OR product.available = :available)
              AND (:categoryId IS NULL OR category.id = :categoryId)
            ORDER BY category.displayOrder ASC, category.name ASC, product.name ASC
            """)
    List<Product> findMenuProducts(
            @Param("categoryId") UUID categoryId,
            @Param("available") Boolean available
    );

    @EntityGraph(attributePaths = {"category"})
    @Query("""
            SELECT product
            FROM Product product
            JOIN product.category category
            WHERE product.active = true
              AND category.active = true
              AND (:available IS NULL OR product.available = :available)
              AND (:categoryId IS NULL OR category.id = :categoryId)
              AND (
                    LOWER(product.name) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(product.description) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(category.name) LIKE LOWER(CONCAT('%', :search, '%'))
              )
            ORDER BY category.displayOrder ASC, category.name ASC, product.name ASC
            """)
    List<Product> searchMenuProducts(
            @Param("search") String search,
            @Param("categoryId") UUID categoryId,
            @Param("available") Boolean available
    );

    @EntityGraph(attributePaths = {"imageUrls", "category"})
    @Query("""
            SELECT product
            FROM Product product
            JOIN product.category category
            WHERE product.id = :id
              AND product.active = true
              AND category.active = true
            """)
    Optional<Product> findPublicById(@Param("id") UUID id);

    @EntityGraph(attributePaths = {"imageUrls", "category"})
    @Query("""
            SELECT product
            FROM Product product
            JOIN product.category category
            WHERE product.id = :id
              AND product.active = true
              AND product.available = true
              AND category.active = true
            """)
    Optional<Product> findAvailableForCartById(@Param("id") UUID id);

    @EntityGraph(attributePaths = {"imageUrls", "category"})
    @Query("SELECT product FROM Product product WHERE product.id = :id")
    Optional<Product> findDetailedById(@Param("id") UUID id);
}
