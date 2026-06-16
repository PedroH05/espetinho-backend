package com.espetinho.api.auth.controller;

import com.espetinho.api.auth.dto.LoginRequest;
import com.espetinho.api.auth.dto.LoginResponse;
import com.espetinho.api.auth.dto.RegisterRequest;
import com.espetinho.api.auth.dto.RegisterResponse;
import com.espetinho.api.auth.service.AuthService;
import com.espetinho.api.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Autenticacao e autorizacao")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Cadastro tradicional", description = "Cria uma conta CLIENT com senha criptografada usando BCrypt.")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = authService.register(request);
        return ResponseEntity.ok(ApiResponse.success("Cadastro realizado com sucesso", response));
    }

    @PostMapping("/login")
    @Operation(summary = "Login tradicional", description = "Autentica o usuario com e-mail e senha e retorna um JWT.")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login realizado com sucesso", response));
    }
}
