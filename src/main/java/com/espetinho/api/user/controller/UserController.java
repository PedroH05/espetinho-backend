package com.espetinho.api.user.controller;

import com.espetinho.api.common.dto.ApiResponse;
import com.espetinho.api.security.CustomUserPrincipal;
import com.espetinho.api.user.dto.UserProfileResponse;
import com.espetinho.api.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Perfil e dados do usuario autenticado")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(
            summary = "Usuario autenticado",
            description = "Retorna os dados basicos do usuario autenticado pelo JWT.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<UserProfileResponse>> me(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        UserProfileResponse response = userService.getAuthenticatedUser(principal);
        return ResponseEntity.ok(ApiResponse.success("Usuario autenticado", response));
    }
}
