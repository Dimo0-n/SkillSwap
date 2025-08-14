package com.example.skillswap.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

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

    @GetMapping("/categories-grid")
    public String categoryGrid(){
        return "categories-grid";
    }

    @GetMapping("/contact")
    public String contact(){
        return "contact";
    }

    @GetMapping("/details-post-default")
    public String detailsPostDefault(){
        return "details-post-default";
    }

    @GetMapping("/details-post-gallery")
    public String detailsPostGallery(){
        return "details-post-gallery";
    }

    @GetMapping("/details-post-review")
    public String detailsPostReview(){
        return "details-post-review";
    }

    @GetMapping("/typography")
    public String typography(){
        return "typography";
    }

}
