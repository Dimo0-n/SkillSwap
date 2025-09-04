package com.example.skillswap.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    //TODO de adaugat o validare de schiluri

    @GetMapping("/index")
    public String home(){
        return "index";
    }

    @GetMapping("/404")
    public String error404(){
        return "404";
    }

    @GetMapping("/categories-list")
    public String categoryList(){
        return "categories-list";
    }

    @GetMapping("/announces-list")
    public String categoryGrid(){
        return "announces-list";
    }

    @GetMapping("/contact")
    public String contact(){
        return "contact";
    }

    @GetMapping("/chat")
    public String detailsPostDefault(){
        return "chat";
    }

    @GetMapping("/post-details")
    public String detailsPostGallery(){
        return "post-details";
    }

    @GetMapping("/profil")
    public String detailsPostReview(){
        return "profil";
    }

    @GetMapping("/typography")
    public String typography(){
        return "typography";
    }

    @GetMapping("/meeting")
    public String meeting(){
        return "jitsi-meet";
    }

}
