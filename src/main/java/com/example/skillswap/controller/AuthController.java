package com.example.skillswap.controller;

import com.example.skillswap.entity.User;
import com.example.skillswap.repository.UserRepository;
import com.example.skillswap.service.impl.AuthServiceImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@Controller
public class AuthController {

    @Autowired
    private AuthServiceImpl authService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @PostMapping("/register/save")
    @ResponseBody
    public ResponseEntity<String> registerUser(@Valid @ModelAttribute User user, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldErrors().stream()
                    .map(fieldError -> fieldError.getDefaultMessage())
                    .findFirst()
                    .orElse("Date invalide.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
        }

        Optional<User> existingUser = authService.searchUserByEmail(user.getEmail());

        // TODO + de verificat daca emailul este valid

        if (existingUser.isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Acest email deja este înregistrat!");
        }

        authService.saveUser(
                user.getEmail(),
                user.getPassword(),
                user.getFullName());

        return ResponseEntity.ok("Cont creat cu succes!");
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

}