package com.espetinho.api.auth.service;

import com.espetinho.api.auth.dto.ForgotPasswordRequest;
import com.espetinho.api.auth.dto.AuthenticatedUserResponse;
import com.espetinho.api.auth.dto.LoginRequest;
import com.espetinho.api.auth.dto.LoginResponse;
import com.espetinho.api.auth.dto.RegisterRequest;
import com.espetinho.api.auth.dto.RegisterResponse;
import com.espetinho.api.auth.dto.ResetPasswordRequest;
import com.espetinho.api.auth.dto.VerifyResetCodeRequest;
import com.espetinho.api.auth.enums.VerificationTokenType;
import com.espetinho.api.auth.token.VerificationTokenService;
import com.espetinho.api.common.exception.BusinessException;
import com.espetinho.api.security.JwtService;
import com.espetinho.api.user.entity.User;
import com.espetinho.api.user.enums.UserAuthProvider;
import com.espetinho.api.user.enums.UserRole;
import com.espetinho.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String INVALID_CREDENTIALS_MESSAGE = "E-mail ou senha invalidos";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final VerificationTokenService verificationTokenService;

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);

        if (userRepository.existsByEmail(email)) {
            throw new BusinessException("E-mail ja cadastrado", HttpStatus.CONFLICT);
        }

        User user = User.builder()
                .name(request.name().trim())
                .email(email)
                .passwordHash(passwordEncoder.encode(request.password()))
                .authProvider(UserAuthProvider.LOCAL)
                .role(UserRole.CLIENT)
                .active(true)
                .emailVerified(true)
                .build();

        User savedUser = userRepository.save(user);

        return new RegisterResponse(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getRole(),
                savedUser.isEmailVerified()
        );
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        User user = userRepository.findByEmail(email)
                .orElseThrow(this::invalidCredentials);

        if (user.getPasswordHash() == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw invalidCredentials();
        }

        if (!user.isActive()) {
            throw new BusinessException("Conta bloqueada ou inativa", HttpStatus.FORBIDDEN);
        }

        if (!user.isEmailVerified()) {
            throw new BusinessException("E-mail ainda nao verificado", HttpStatus.FORBIDDEN);
        }

        String token = jwtService.generateToken(user);

        return new LoginResponse(
                token,
                new AuthenticatedUserResponse(
                        user.getId(),
                        user.getName(),
                        user.getEmail(),
                        user.getRole()
                )
        );
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        String email = normalizeEmail(request.email());
        userRepository.findByEmail(email)
                .filter(User::isActive)
                .filter(user -> user.getAuthProvider() == UserAuthProvider.LOCAL)
                .ifPresent(verificationTokenService::createPasswordResetCode);
    }

    @Transactional(readOnly = true)
    public void verifyResetCode(VerifyResetCodeRequest request) {
        verificationTokenService.validateCode(
                normalizeEmail(request.email()),
                VerificationTokenType.PASSWORD_RESET,
                request.code()
        );
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        String email = normalizeEmail(request.email());
        User user = userRepository.findByEmail(email)
                .orElseThrow(this::invalidResetCode);

        if (user.getAuthProvider() != UserAuthProvider.LOCAL) {
            throw invalidResetCode();
        }

        verificationTokenService.consumeCode(email, VerificationTokenType.PASSWORD_RESET, request.code());
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    private BusinessException invalidCredentials() {
        return new BusinessException(INVALID_CREDENTIALS_MESSAGE, HttpStatus.UNAUTHORIZED);
    }

    private BusinessException invalidResetCode() {
        return new BusinessException("Codigo invalido ou expirado", HttpStatus.BAD_REQUEST);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
