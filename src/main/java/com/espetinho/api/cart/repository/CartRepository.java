package com.espetinho.api.cart.repository;

import com.espetinho.api.cart.entity.Cart;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CartRepository extends JpaRepository<Cart, UUID> {

    @EntityGraph(attributePaths = {"items", "items.product"})
    @Query("""
            SELECT cart
            FROM Cart cart
            WHERE cart.user.id = :userId
              AND cart.active = true
            """)
    Optional<Cart> findActiveByUserId(@Param("userId") UUID userId);

    @EntityGraph(attributePaths = {"items", "items.product"})
    @Query("""
            SELECT cart
            FROM Cart cart
            WHERE cart.guestId = :guestId
              AND cart.active = true
            """)
    Optional<Cart> findActiveByGuestId(@Param("guestId") UUID guestId);
}
