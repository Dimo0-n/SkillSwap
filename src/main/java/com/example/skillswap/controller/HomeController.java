package com.example.skillswap.controller;

import com.example.skillswap.entity.Announce;
import com.example.skillswap.sevice.AnnounceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    //TODO de adaugat o validare de schiluri

    @Autowired
    private AnnounceService announceService;

    @GetMapping("/index")
    public String home(Model model){
        List<Announce> latest5Announces = announceService.getLatest5Announces();
        model.addAttribute("latest5Announces", latest5Announces);
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
