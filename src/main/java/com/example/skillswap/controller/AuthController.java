package com.example.skillswap.controller;

import com.example.skillswap.repository.UserRepository;
import com.example.skillswap.sevice.AuthService;
import com.example.skillswap.sevice.AuthServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    @Autowired
    private AuthServiceImpl authService;



}
