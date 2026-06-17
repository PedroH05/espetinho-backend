package com.espetinho.api.product.controller;

import com.espetinho.api.common.dto.ApiResponse;
import com.espetinho.api.product.dto.ProductMenuResponse;
import com.espetinho.api.product.dto.ProductRequest;
import com.espetinho.api.product.dto.ProductResponse;
import com.espetinho.api.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Cardapio publico")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @Operation(
            summary = "Listar cardapio",
            description = "Retorna produtos ativos e categorias prontas para a tela de cardapio. Aceita filtros por busca, categoria e disponibilidade."
    )
    public ResponseEntity<ApiResponse<ProductMenuResponse>> listProducts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(defaultValue = "true") Boolean available
    ) {
        ProductMenuResponse response = productService.getMenu(search, categoryId, available);
        return ResponseEntity.ok(ApiResponse.success("Cardapio consultado com sucesso", response));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Buscar produto por ID",
            description = "Retorna os dados detalhados de um produto ativo."
    )
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(@PathVariable UUID id) {
        ProductResponse response = productService.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Produto consultado com sucesso", response));
    }

    @PostMapping
    @Operation(
            summary = "Criar produto",
            description = "Cria um produto vinculado a uma categoria ativa. Acesso ADMIN.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @Valid @RequestBody ProductRequest request
    ) {
        ProductResponse response = productService.create(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Produto criado com sucesso", response));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Atualizar produto",
            description = "Atualiza os dados principais de um produto. Acesso ADMIN.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable UUID id,
            @Valid @RequestBody ProductRequest request
    ) {
        ProductResponse response = productService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Produto atualizado com sucesso", response));
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(
            summary = "Indisponibilizar produto",
            description = "Marca um produto como indisponivel temporariamente, sem remove-lo do catalogo administrativo. Acesso ADMIN.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<Void>> deactivateProduct(@PathVariable UUID id) {
        productService.deactivate(id);
        return ResponseEntity.ok(ApiResponse.<Void>success("Produto indisponibilizado com sucesso", null));
    }

    @PatchMapping("/{id}/activate")
    @Operation(
            summary = "Ativar produto",
            description = "Restaura um produto e marca como disponivel. Acesso ADMIN.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<ProductResponse>> activateProduct(@PathVariable UUID id) {
        ProductResponse response = productService.activate(id);
        return ResponseEntity.ok(ApiResponse.success("Produto ativado com sucesso", response));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Remover produto",
            description = "Faz soft delete: marca active=false, sem apagar o produto do banco. Acesso ADMIN.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable UUID id) {
        productService.softDelete(id);
        return ResponseEntity.ok(ApiResponse.<Void>success("Produto removido com sucesso", null));
    }
}
