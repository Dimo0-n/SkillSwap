package com.example.skillswap.error;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class ErrorsController {

    @GetMapping("/error/404")
    public String errorNotFound(){
        return "404";
    }

    @RequestMapping(value = "/error/403", method = {RequestMethod.GET, RequestMethod.POST})
    public String accessDenied() {
        return "403";
    }

}
