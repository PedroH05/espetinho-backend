package com.espetinho.api.user.service;

import com.espetinho.api.security.CustomUserPrincipal;
import com.espetinho.api.user.dto.UserProfileResponse;
import com.espetinho.api.user.entity.User;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    public UserProfileResponse getAuthenticatedUser(CustomUserPrincipal principal) {
        User user = principal.getUser();

        return new UserProfileResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );
    }
}
