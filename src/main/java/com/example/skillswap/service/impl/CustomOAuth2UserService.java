package com.example.skillswap.service.impl;

import com.example.skillswap.entity.Role;
import com.example.skillswap.entity.User;
import com.example.skillswap.enums.AuthProvider;
import com.example.skillswap.repository.RoleRepository;
import com.example.skillswap.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService
        extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest request)
            throws OAuth2AuthenticationException {

        OAuth2User oauthUser = super.loadUser(request);

        String email = oauthUser.getAttribute("email");
        String name = oauthUser.getAttribute("name");

        if (email == null || email.isBlank()) {
            throw new OAuth2AuthenticationException(new OAuth2Error("invalid_user_info"),
                    "Google account did not provide an email address");
        }

        User user = userRepository.findByEmail(email)
            .orElseGet(() -> registerNewUser(email, name));

        Map<String, Object> attributes = new HashMap<>(oauthUser.getAttributes());
        attributes.put("userId", user.getId());

        return new DefaultOAuth2User(
                user.getRoles().stream()
                        .map(r -> new SimpleGrantedAuthority(r.getName()))
                        .toList(),
            attributes,
                "email"
        );
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

        return userRepository.save(user);
    }
}
