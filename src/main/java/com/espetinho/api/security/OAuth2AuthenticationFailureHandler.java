package com.espetinho.api.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class OAuth2AuthenticationFailureHandler implements AuthenticationFailureHandler {

    private final String frontendBaseUrl;
    private final String googleFailurePath;

    public OAuth2AuthenticationFailureHandler(
            @Value("${app.frontend.base-url}") String frontendBaseUrl,
            @Value("${app.frontend.google-failure-path}") String googleFailurePath
    ) {
        this.frontendBaseUrl = frontendBaseUrl;
        this.googleFailurePath = googleFailurePath;
    }

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException, ServletException {
        String error = URLEncoder.encode("google_login_failed", StandardCharsets.UTF_8);
        response.sendRedirect(normalizeUrl(frontendBaseUrl, googleFailurePath) + "?error=" + error);
    }

    private String normalizeUrl(String baseUrl, String path) {
        String normalizedBaseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        String normalizedPath = path.startsWith("/") ? path : "/" + path;

        return normalizedBaseUrl + normalizedPath;
    }
}
