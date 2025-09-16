package com.example.skillswap.sevice;

import com.example.skillswap.entity.Role;
import com.example.skillswap.entity.User;
import com.example.skillswap.repository.RoleRepository;
import com.example.skillswap.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public void saveUser(String email, String password, String fullName) {
        User newUser = new User();

        newUser.setPassword(passwordEncoder.encode(password));
        newUser.setEmail(email);
        newUser.setFullName(fullName);
        newUser.setRegisterData(LocalDateTime.now());

        Role role = roleRepository.findByName("ROLE_USER");
        newUser.setRoles(Arrays.asList(role));

        userRepository.save(newUser);

    }

    @Override
    public Optional<User> searchUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

}
