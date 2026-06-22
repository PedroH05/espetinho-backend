package com.espetinho.api.user.service;

import com.espetinho.api.security.CustomUserPrincipal;
import com.espetinho.api.user.dto.UpdateUserProfileRequest;
import com.espetinho.api.user.dto.UserProfileResponse;
import com.espetinho.api.user.entity.User;
import com.espetinho.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserProfileResponse getAuthenticatedUser(CustomUserPrincipal principal) {
        User user = principal.getUser();

        return toProfileResponse(user);
    }

    @Transactional
    public UserProfileResponse updateAuthenticatedUser(
            CustomUserPrincipal principal,
            UpdateUserProfileRequest request
    ) {
        User user = principal.getUser();
        user.setName(request.name().trim());
        User updatedUser = userRepository.save(user);

        return toProfileResponse(updatedUser);
    }

    private UserProfileResponse toProfileResponse(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                new LinkedHashSet<>(user.getRoles())
        );
    }
}
