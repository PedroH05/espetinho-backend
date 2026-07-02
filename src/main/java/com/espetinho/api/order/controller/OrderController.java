package com.espetinho.api.order.controller;

import com.espetinho.api.common.dto.ApiResponse;
import com.espetinho.api.order.dto.CreateOrderRequest;
import com.espetinho.api.order.dto.OrderResponse;
import com.espetinho.api.order.service.OrderService;
import com.espetinho.api.security.CustomUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Pedidos de delivery, retirada e consumo no local")
public class OrderController {

    private static final String GUEST_ID_HEADER = "X-Guest-Id";

    private final OrderService orderService;

    @PostMapping
    @Operation(
            summary = "Criar pedido",
            description = "Cria um pedido a partir do carrinho ativo do usuario logado ou visitante."
    )
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @Parameter(description = "UUID do visitante quando nao houver login")
            @RequestHeader(value = GUEST_ID_HEADER, required = false) String guestId,
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody CreateOrderRequest request
    ) {
        OrderResponse response = orderService.createOrder(guestId, principal, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Pedido criado com sucesso", response));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Buscar pedido por ID",
            description = "Retorna pedido completo. Visitante precisa enviar o mesmo X-Guest-Id usado na compra."
    )
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(
            @PathVariable UUID id,
            @Parameter(description = "UUID do visitante quando nao houver login")
            @RequestHeader(value = GUEST_ID_HEADER, required = false) String guestId,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        OrderResponse response = orderService.getOrderById(id, guestId, principal);
        return ResponseEntity.ok(ApiResponse.success("Pedido consultado com sucesso", response));
    }
}
