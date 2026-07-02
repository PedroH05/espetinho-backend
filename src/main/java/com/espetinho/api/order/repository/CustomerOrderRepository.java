package com.espetinho.api.order.repository;

import com.espetinho.api.order.entity.CustomerOrder;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, UUID> {

    @EntityGraph(attributePaths = {"user", "items", "items.product"})
    @Query("""
            SELECT customerOrder
            FROM CustomerOrder customerOrder
            WHERE customerOrder.id = :id
            """)
    Optional<CustomerOrder> findDetailedById(@Param("id") UUID id);
}
