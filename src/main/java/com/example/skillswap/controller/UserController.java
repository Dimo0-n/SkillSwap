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

    @GetMapping("/login")
    public String login(){
        return "redirect:/index";
    }

    @PostMapping("/register")
    @ResponseBody
    public ResponseEntity<?> register(@RequestParam String password,
                                      @RequestParam String confirmPassword,
                                      @RequestParam String email,
                                      @RequestParam String fullName,
                                      @RequestParam (required = false) String termsAndConditions) {
        if (termsAndConditions != null) {

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
        else{
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", "Acceptati termenile si conditiile"));
        }
    }


}
