package com.example.skillswap.controller;

import com.example.skillswap.entity.User;
import com.example.skillswap.repository.UserRepository;
import com.example.skillswap.service.impl.AuthServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
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
    public String register(){
        return "register";
    }

    @PostMapping("/register/save")
    @ResponseBody
    public ResponseEntity<String> registerUser(@RequestParam Map<String, String> allRequestParams) {
        Optional<User> user = authService.searchUserByEmail(allRequestParams.get("email"));

        if (user.isPresent()) {
            return ResponseEntity.badRequest().body("Acest email deja este înregistrat!");
        }

        authService.saveUser(
                allRequestParams.get("email"),
                allRequestParams.get("password"),
                allRequestParams.get("fullName")
        );

        return ResponseEntity.ok("Cont creat cu succes!");
    }

    @GetMapping("/login")
    public String login(){
        return "login";
    }

}