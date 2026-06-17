package com.espetinho.api.product.dto;

import com.espetinho.api.category.dto.CategoryResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Dados do cardapio prontos para consumo pelo frontend")
public record ProductMenuResponse(
        List<CategoryResponse> categories,
        List<ProductResponse> products
) {
}
