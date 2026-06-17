package com.espetinho.api.auth.token;

import com.espetinho.api.auth.entity.VerificationToken;
import com.espetinho.api.auth.enums.VerificationTokenType;
import com.espetinho.api.auth.repository.VerificationTokenRepository;
import com.espetinho.api.common.email.EmailService;
import com.espetinho.api.common.exception.BusinessException;
import com.espetinho.api.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class VerificationTokenService {

    private static final Duration TOKEN_EXPIRATION = Duration.ofMinutes(15);
    private static final int CODE_LIMIT = 1_000_000;
    private static final String INVALID_CODE_MESSAGE = "Codigo invalido ou expirado";

    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final SecureRandom secureRandom = new SecureRandom();

    public void createPasswordResetCode(User user) {
        String email = normalizeEmail(user.getEmail());
        invalidateActiveCodes(email, VerificationTokenType.PASSWORD_RESET);

        String code = generateCode();
        VerificationToken token = VerificationToken.builder()
                .user(user)
                .email(email)
                .type(VerificationTokenType.PASSWORD_RESET)
                .codeHash(passwordEncoder.encode(code))
                .expiresAt(Instant.now().plus(TOKEN_EXPIRATION))
                .build();

        verificationTokenRepository.save(token);
        emailService.sendPasswordResetCode(email, code);
    }

    public void validateCode(String email, VerificationTokenType type, String code) {
        VerificationToken token = getLatestActiveCode(email, type);

        if (token.getExpiresAt().isBefore(Instant.now()) || !passwordEncoder.matches(code, token.getCodeHash())) {
            throw invalidCode();
        }
    }

    public void consumeCode(String email, VerificationTokenType type, String code) {
        VerificationToken token = getLatestActiveCode(email, type);

        if (token.getExpiresAt().isBefore(Instant.now()) || !passwordEncoder.matches(code, token.getCodeHash())) {
            throw invalidCode();
        }

        token.setUsedAt(Instant.now());
        verificationTokenRepository.save(token);
    }

    private VerificationToken getLatestActiveCode(String email, VerificationTokenType type) {
        return verificationTokenRepository
                .findFirstByEmailAndTypeAndUsedAtIsNullOrderByCreatedAtDesc(normalizeEmail(email), type)
                .orElseThrow(this::invalidCode);
    }

    private void invalidateActiveCodes(String email, VerificationTokenType type) {
        Instant now = Instant.now();
        var activeTokens = verificationTokenRepository.findAllByEmailAndTypeAndUsedAtIsNull(email, type);
        activeTokens.forEach(token -> token.setUsedAt(now));
        verificationTokenRepository.saveAll(activeTokens);
    }

    private String generateCode() {
        return String.format("%06d", secureRandom.nextInt(CODE_LIMIT));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private BusinessException invalidCode() {
        return new BusinessException(INVALID_CODE_MESSAGE, HttpStatus.BAD_REQUEST);
    }
}
