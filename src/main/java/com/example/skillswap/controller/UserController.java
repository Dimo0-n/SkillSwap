package com.example.skillswap.controller;

import com.example.skillswap.entity.User;
import com.example.skillswap.repository.UserRepository;
import com.example.skillswap.sevice.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;
import java.util.Optional;

@Controller
public class UserController {

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/register")
    public String register(){
        return "register";
    }

    @PostMapping("/register/save")
    @ResponseBody
    public ResponseEntity<String> registerUser(@RequestParam Map<String, String> allRequestParams) {
        Optional<User> user = userService.searchUserByEmail(allRequestParams.get("email"));

        if (user.isPresent()) {
            return ResponseEntity.badRequest().body("Acest email deja este înregistrat!");
        }

        userService.saveUser(
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