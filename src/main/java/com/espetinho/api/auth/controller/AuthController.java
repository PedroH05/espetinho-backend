package com.espetinho.api.auth.controller;

import com.espetinho.api.auth.dto.ForgotPasswordRequest;
import com.espetinho.api.auth.dto.LoginRequest;
import com.espetinho.api.auth.dto.LoginResponse;
import com.espetinho.api.auth.dto.RegisterRequest;
import com.espetinho.api.auth.dto.RegisterResponse;
import com.espetinho.api.auth.dto.ResetPasswordRequest;
import com.espetinho.api.auth.dto.VerifyResetCodeRequest;
import com.espetinho.api.auth.service.AuthService;
import com.espetinho.api.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

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

    @PostMapping("/forgot-password")
    @Operation(summary = "Solicitar recuperacao de senha", description = "Gera um codigo de recuperacao para o e-mail informado.")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.<Void>success("Se o e-mail existir, enviaremos um codigo de recuperacao", null));
    }

    @PostMapping("/verify-reset-code")
    @Operation(summary = "Validar codigo de recuperacao", description = "Verifica se o codigo de recuperacao ainda esta valido.")
    public ResponseEntity<ApiResponse<Void>> verifyResetCode(@Valid @RequestBody VerifyResetCodeRequest request) {
        authService.verifyResetCode(request);
        return ResponseEntity.ok(ApiResponse.<Void>success("Codigo valido", null));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Redefinir senha", description = "Atualiza a senha usando um codigo de recuperacao valido.")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.<Void>success("Senha alterada com sucesso", null));
    }

    @GetMapping("/google")
    @Operation(summary = "Login com Google", description = "Redireciona o usuario para o fluxo OAuth2 do Google.")
    public ResponseEntity<Void> googleLogin() {
        return ResponseEntity
                .status(HttpStatus.FOUND)
                .location(URI.create("/oauth2/authorization/google"))
                .build();
    }
}
