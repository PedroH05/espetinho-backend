package com.espetinho.api.auth.repository;

import com.espetinho.api.auth.entity.VerificationToken;
import com.espetinho.api.auth.enums.VerificationTokenType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, UUID> {

    List<VerificationToken> findAllByEmailAndTypeAndUsedAtIsNull(String email, VerificationTokenType type);

    Optional<VerificationToken> findFirstByEmailAndTypeAndUsedAtIsNullOrderByCreatedAtDesc(
            String email,
            VerificationTokenType type
    );
}
