package com.example.skillswap.controller;

import com.example.skillswap.entity.User;
import com.example.skillswap.repository.UserRepository;
import com.example.skillswap.sevice.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@Controller
public class UserController {

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private UserRepository userRepository;

    // Endpoint pentru SUCCESS după login (apelat de AJAX)
    @PostMapping("/login-success")
    @ResponseBody
    public Map<String, String> loginSuccess() {
        return Map.of("success", "Login realizat cu succes!");
    }

    // Endpoint pentru EROARE de login (apelat de AJAX)
    @PostMapping("/login-error")
    @ResponseBody
    public ResponseEntity<?> loginError() {
        return ResponseEntity
                .badRequest()
                .body(Map.of("error", "Datele introduse sunt greșite!"));
    }

    @PostMapping("/register")
    @ResponseBody
    public ResponseEntity<?> register(@RequestParam String password,
                                      @RequestParam String confirmPassword,
                                      @RequestParam String email,
                                      @RequestParam String fullName) {

        if (!password.equals(confirmPassword)) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", "Parolele nu coincid, boss!"));
        }

        Optional<User> existingUser = userRepository.findByEmail(email);

        if (existingUser.isPresent()) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", "Email-ul e deja folosit, baga altul!"));
        }

        userService.saveUser(email, password, fullName);

        return ResponseEntity.ok(Map.of("success", "Te-ai înregistrat cu succes!"));

    }
}