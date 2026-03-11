package com.example.skillswap.service.impl;

import com.example.skillswap.entity.Role;
import com.example.skillswap.entity.User;
import com.example.skillswap.enums.AuthProvider;
import com.example.skillswap.repository.RoleRepository;
import com.example.skillswap.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CustomOidcUserService extends OidcUserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);

        String email = oidcUser.getEmail();
        String name = oidcUser.getFullName();

        if (email == null || email.isBlank()) {
            throw new OAuth2AuthenticationException(new OAuth2Error("invalid_user_info"),
                    "Google account did not provide an email address");
        }

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> registerNewUser(email, name));

        return new DefaultOidcUser(
                user.getRoles().stream()
                        .map(r -> new SimpleGrantedAuthority(r.getName()))
                        .toList(),
                oidcUser.getIdToken(),
                oidcUser.getUserInfo(),
                "email");
    }

    private User registerNewUser(String email, String name) {
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_USER")));

        User user = new User();
        user.setEmail(email);
        user.setFullName((name == null || name.isBlank()) ? email : name);
        user.setProvider(AuthProvider.GOOGLE);
        user.setRegisterData(LocalDateTime.now());
        user.setRoles(Set.of(userRole));
        user.setPassword(null);
        user.setProfileCompleted(false);

        User savedUser = userRepository.save(user);
        notificationService.createWelcomeNotification(savedUser.getId());
        return savedUser;
    }
}
