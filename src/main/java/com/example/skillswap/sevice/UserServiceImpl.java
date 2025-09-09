package com.example.skillswap.sevice;

import com.example.skillswap.entity.User;
import com.example.skillswap.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public void saveUser(String username, String password, String email, String fullName) {
        User newUser = new User();

        if (username.isEmpty())
            newUser.setUsername(null);
        else
            newUser.setUsername(username);
        newUser.setPassword(passwordEncoder.encode(password));
        newUser.setEmail(email);
        newUser.setFullName(fullName);
        newUser.setRegisterData(LocalDateTime.now());

        userRepository.save(newUser);

    }

}
