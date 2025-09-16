package com.example.skillswap.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
class AdminController {

    @GetMapping("/admin")
    public String adminMain(){
        return "admin-main";
    }

}
