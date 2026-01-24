package com.example.skillswap.service;

import com.example.skillswap.entity.User;

import java.util.Optional;

public interface AuthService {

    void saveUser(String email, String password, String fullName);

    Optional<User> searchUserByEmail(String email);
}
