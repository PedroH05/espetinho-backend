package com.espetinho.api.cart.controller;

import com.espetinho.api.cart.dto.AddCartItemRequest;
import com.espetinho.api.cart.dto.CartResponse;
import com.espetinho.api.cart.dto.UpdateCartItemRequest;
import com.espetinho.api.cart.service.CartService;
import com.espetinho.api.common.dto.ApiResponse;
import com.espetinho.api.security.CustomUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@Tag(name = "Cart", description = "Carrinho de compras para usuarios logados e visitantes")
public class CartController {

    private static final String GUEST_ID_HEADER = "X-Guest-Id";

    private final CartService cartService;

    @GetMapping
    @Operation(
            summary = "Consultar carrinho",
            description = "Retorna o carrinho do usuario autenticado ou do visitante identificado pelo header X-Guest-Id."
    )
    public ResponseEntity<ApiResponse<CartResponse>> getCart(
            @Parameter(description = "UUID do visitante quando nao houver login")
            @RequestHeader(value = GUEST_ID_HEADER, required = false) String guestId,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        CartResponse response = cartService.getCart(guestId, principal);
        return ResponseEntity.ok(ApiResponse.success("Carrinho consultado com sucesso", response));
    }

    @PostMapping("/items")
    @Operation(
            summary = "Adicionar item ao carrinho",
            description = "Adiciona produto ao carrinho. Se o produto ja existir, soma a quantidade."
    )
    public ResponseEntity<ApiResponse<CartResponse>> addItem(
            @Parameter(description = "UUID do visitante quando nao houver login")
            @RequestHeader(value = GUEST_ID_HEADER, required = false) String guestId,
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody AddCartItemRequest request
    ) {
        CartResponse response = cartService.addItem(guestId, principal, request);
        return ResponseEntity.ok(ApiResponse.success("Item adicionado ao carrinho", response));
    }

    @PutMapping("/items/{itemId}")
    @Operation(
            summary = "Alterar quantidade de item",
            description = "Atualiza a quantidade de um item do carrinho."
    )
    public ResponseEntity<ApiResponse<CartResponse>> updateItem(
            @Parameter(description = "UUID do visitante quando nao houver login")
            @RequestHeader(value = GUEST_ID_HEADER, required = false) String guestId,
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID itemId,
            @Valid @RequestBody UpdateCartItemRequest request
    ) {
        CartResponse response = cartService.updateItem(guestId, principal, itemId, request);
        return ResponseEntity.ok(ApiResponse.success("Item atualizado com sucesso", response));
    }

    @DeleteMapping("/items/{itemId}")
    @Operation(
            summary = "Remover item do carrinho",
            description = "Remove um item especifico do carrinho."
    )
    public ResponseEntity<ApiResponse<CartResponse>> removeItem(
            @Parameter(description = "UUID do visitante quando nao houver login")
            @RequestHeader(value = GUEST_ID_HEADER, required = false) String guestId,
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID itemId
    ) {
        CartResponse response = cartService.removeItem(guestId, principal, itemId);
        return ResponseEntity.ok(ApiResponse.success("Item removido do carrinho", response));
    }

    @DeleteMapping
    @Operation(
            summary = "Limpar carrinho",
            description = "Remove todos os itens do carrinho."
    )
    public ResponseEntity<ApiResponse<CartResponse>> clearCart(
            @Parameter(description = "UUID do visitante quando nao houver login")
            @RequestHeader(value = GUEST_ID_HEADER, required = false) String guestId,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        CartResponse response = cartService.clearCart(guestId, principal);
        return ResponseEntity.ok(ApiResponse.success("Carrinho limpo com sucesso", response));
    }
}
