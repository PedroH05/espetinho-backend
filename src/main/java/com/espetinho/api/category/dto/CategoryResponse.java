package com.espetinho.api.category.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Categoria de produtos")
public record CategoryResponse(
        UUID id,
        String name,
        String slug,
        int displayOrder
) {
}
