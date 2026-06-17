package com.espetinho.api.product.dto;

import com.espetinho.api.category.dto.CategoryResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Schema(description = "Produto retornado para exibicao no cardapio")
public record ProductResponse(
        UUID id,
        String name,
        String description,
        BigDecimal price,
        CategoryResponse category,
        List<String> imageUrls,
        boolean available,
        Integer stockQuantity
) {
}
