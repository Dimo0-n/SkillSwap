package com.example.skillswap.sevice;

import com.example.skillswap.entity.User;

import java.util.Optional;

public interface UserService {

    void saveUser(String email, String password, String fullName);

    Optional<User> searchUserByEmail(String email);
}
