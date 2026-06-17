package com.espetinho.api.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Schema(description = "Dados para criacao ou atualizacao de produto")
public record ProductRequest(
        @Schema(example = "Espetinho de Carne")
        @NotBlank(message = "Nome e obrigatorio")
        @Size(max = 120, message = "Nome deve ter no maximo 120 caracteres")
        String name,

        @Schema(example = "Espetinho de carne bovina temperada")
        @NotBlank(message = "Descricao e obrigatoria")
        @Size(max = 1000, message = "Descricao deve ter no maximo 1000 caracteres")
        String description,

        @Schema(example = "9.90")
        @NotNull(message = "Preco e obrigatorio")
        @DecimalMin(value = "0.00", message = "Preco deve ser maior ou igual a zero")
        BigDecimal price,

        @NotNull(message = "Categoria e obrigatoria")
        UUID categoryId,

        @Schema(example = "[\"https://res.cloudinary.com/demo/image/upload/espetinho.jpg\"]")
        @Size(max = 10, message = "Produto deve ter no maximo 10 imagens")
        List<
                @NotBlank(message = "URL da imagem nao pode ser vazia")
                @Size(max = 500, message = "URL da imagem deve ter no maximo 500 caracteres")
                String
        > imageUrls,

        @Schema(example = "true")
        Boolean available,

        @Schema(example = "20")
        @DecimalMin(value = "0", message = "Estoque deve ser maior ou igual a zero")
        Integer stockQuantity
) {
}
