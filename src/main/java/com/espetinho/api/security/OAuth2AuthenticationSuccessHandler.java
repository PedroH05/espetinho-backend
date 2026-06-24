package com.espetinho.api.security;

import com.espetinho.api.auth.dto.LoginResponse;
import com.espetinho.api.auth.service.GoogleOAuth2Service;
import com.espetinho.api.common.exception.BusinessException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final GoogleOAuth2Service googleOAuth2Service;

    @Value("${app.frontend.base-url}")
    private String frontendBaseUrl;

    @Value("${app.frontend.google-success-path}")
    private String googleSuccessPath;

    @Value("${app.frontend.google-failure-path}")
    private String googleFailurePath;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        try {
            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
            LoginResponse loginResponse = googleOAuth2Service.authenticate(oauth2User);
            String token = URLEncoder.encode(loginResponse.token(), StandardCharsets.UTF_8);

            response.sendRedirect(normalizeUrl(frontendBaseUrl, googleSuccessPath) + "?token=" + token);
        } catch (BusinessException exception) {
            String error = URLEncoder.encode(exception.getMessage(), StandardCharsets.UTF_8);
            response.sendRedirect(normalizeUrl(frontendBaseUrl, googleFailurePath) + "?error=" + error);
        }
    }

    private String normalizeUrl(String baseUrl, String path) {
        String normalizedBaseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        String normalizedPath = path.startsWith("/") ? path : "/" + path;

        return normalizedBaseUrl + normalizedPath;
    }
}
