package com.example.skillswap.service.impl;

import com.example.skillswap.entity.Role;
import com.example.skillswap.entity.User;
import com.example.skillswap.repository.RoleRepository;
import com.example.skillswap.repository.UserRepository;
import com.example.skillswap.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthServiceImpl(
            UserRepository userRepository,
            RoleRepository roleRepository,
            BCryptPasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void saveUser(String email, String password, String fullName) {

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() ->
                        new IllegalStateException("ROLE_USER not found in database")
                );

        User newUser = new User();
        newUser.setEmail(email);
        newUser.setFullName(fullName);
        newUser.setPassword(passwordEncoder.encode(password));
        newUser.setRegisterData(LocalDateTime.now());

        newUser.getRoles().add(userRole);

        userRepository.save(newUser);
    }

    @Override
    public Optional<User> searchUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}

