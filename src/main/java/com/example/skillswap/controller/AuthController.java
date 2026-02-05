package com.example.skillswap.controller;

import com.example.skillswap.entity.User;
import com.example.skillswap.repository.UserRepository;
import com.example.skillswap.service.impl.AuthServiceImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
    public String registerUser(@Valid @ModelAttribute User user, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "register";
        }

        Optional<User> existingUser = authService.searchUserByEmail(user.getEmail());

        if (existingUser.isPresent()) {
            model.addAttribute("error", "Acest email deja este înregistrat!");
            return "register";
        }

        authService.saveUser(
                user.getEmail(),
                user.getPassword(),
                user.getFullName());

        return "redirect:/login?success";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

}