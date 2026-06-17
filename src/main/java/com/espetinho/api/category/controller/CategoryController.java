package com.espetinho.api.category.controller;

import com.espetinho.api.category.dto.CategoryResponse;
import com.espetinho.api.category.service.CategoryService;
import com.espetinho.api.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Categorias do cardapio")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    @Operation(
            summary = "Listar categorias",
            description = "Retorna categorias ativas para filtros do cardapio."
    )
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> listCategories() {
        List<CategoryResponse> response = categoryService.listActiveCategories();
        return ResponseEntity.ok(ApiResponse.success("Categorias consultadas com sucesso", response));
    }
}
