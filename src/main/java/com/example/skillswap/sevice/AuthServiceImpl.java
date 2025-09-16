package com.example.skillswap.sevice;

import com.example.skillswap.entity.User;
import com.example.skillswap.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public void saveUser(String email, String password, String fullName) {
        User newUser = new User();

        newUser.setPassword(passwordEncoder.encode(password));
        newUser.setEmail(email);
        newUser.setFullName(fullName);
        newUser.setRegisterData(LocalDateTime.now());

        userRepository.save(newUser);

    }

    @Override
    public Optional<User> searchUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

}
