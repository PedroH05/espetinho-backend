package com.espetinho.api.auth.service;

import com.espetinho.api.auth.dto.AuthenticatedUserResponse;
import com.espetinho.api.auth.dto.LoginResponse;
import com.espetinho.api.common.exception.BusinessException;
import com.espetinho.api.security.JwtService;
import com.espetinho.api.user.entity.User;
import com.espetinho.api.user.enums.UserAuthProvider;
import com.espetinho.api.user.enums.UserRole;
import com.espetinho.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class GoogleOAuth2Service {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Transactional
    public LoginResponse authenticate(OAuth2User oauth2User) {
        String googleId = requiredAttribute(oauth2User, "sub");
        String email = requiredAttribute(oauth2User, "email").trim().toLowerCase();
        String name = requiredAttribute(oauth2User, "name").trim();
        String picture = oauth2User.getAttribute("picture");
        Boolean emailVerified = oauth2User.getAttribute("email_verified");

        if (!Boolean.TRUE.equals(emailVerified)) {
            throw new BusinessException("E-mail Google ainda nao verificado", HttpStatus.FORBIDDEN);
        }

        User user = userRepository.findByGoogleId(googleId)
                .or(() -> userRepository.findByEmail(email))
                .map(existingUser -> updateGoogleData(existingUser, googleId, name, picture))
                .orElseGet(() -> createGoogleUser(googleId, email, name, picture));

        if (!user.isActive()) {
            throw new BusinessException("Conta bloqueada", HttpStatus.FORBIDDEN);
        }

        String token = jwtService.generateToken(user);

        return new LoginResponse(
                token,
                new AuthenticatedUserResponse(
                        user.getId(),
                        user.getName(),
                        user.getEmail(),
                        copyRoles(user)
                )
        );
    }

    private User updateGoogleData(User user, String googleId, String name, String picture) {
        if (user.getGoogleId() != null && !user.getGoogleId().equals(googleId)) {
            throw new BusinessException("E-mail ja vinculado a outra conta Google", HttpStatus.CONFLICT);
        }

        user.setGoogleId(googleId);
        user.setName(name);
        user.setAvatarUrl(picture);
        user.setEmailVerified(true);

        return userRepository.save(user);
    }

    private User createGoogleUser(String googleId, String email, String name, String picture) {
        User user = User.builder()
                .name(name)
                .email(email)
                .googleId(googleId)
                .avatarUrl(picture)
                .passwordHash(null)
                .authProvider(UserAuthProvider.GOOGLE)
                .roles(defaultClientRoles())
                .active(true)
                .emailVerified(true)
                .build();

        return userRepository.save(user);
    }

    private String requiredAttribute(OAuth2User oauth2User, String attributeName) {
        String value = oauth2User.getAttribute(attributeName);

        if (value == null || value.isBlank()) {
            throw new BusinessException("Google nao retornou o campo " + attributeName, HttpStatus.BAD_REQUEST);
        }

        return value;
    }

    private Set<UserRole> defaultClientRoles() {
        return new LinkedHashSet<>(Set.of(UserRole.CLIENT));
    }

    private Set<UserRole> copyRoles(User user) {
        return new LinkedHashSet<>(user.getRoles());
    }
}
