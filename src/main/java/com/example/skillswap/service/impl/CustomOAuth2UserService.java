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
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

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
    public OAuth2User loadUser(OAuth2UserRequest request)
            throws OAuth2AuthenticationException {

        OAuth2User oauthUser = super.loadUser(request);

        String email = oauthUser.getAttribute("email");
        String name = oauthUser.getAttribute("name");

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
                .orElseThrow();

        User user = new User();
        user.setEmail(email);
        user.setFullName(name);
        user.setProvider(AuthProvider.GOOGLE);
        user.setRegisterData(LocalDateTime.now());
        user.setRoles(Set.of(userRole));
        user.setPassword(null);

        return userRepository.save(user);
    }
}
