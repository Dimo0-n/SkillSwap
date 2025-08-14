package com.example.skillswap.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/index")
    public String home(){
        return "index";
    }

    @GetMapping("/categoryList")
    public String categoryList(){
        return "categories-list";
    }

}
