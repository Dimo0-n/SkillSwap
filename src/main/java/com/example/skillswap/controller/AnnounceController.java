package com.example.skillswap.controller;

import com.example.skillswap.entity.Announce;
import com.example.skillswap.sevice.AnnounceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
class AnnounceController {

    @Autowired
    private AnnounceService announceService;

    @GetMapping("/announces-list")
    public String categoryGrid(Model model){
        List<Announce> announcesList = announceService.getAnnouncesList();
        model.addAttribute("announcesList", announcesList);
        model.addAttribute("page", "annouces-list" );
        return "announces-list";
    }

    @GetMapping("/announces/new")
    public String addAnnounce(Model model){
        Announce announce = new Announce();
        model.addAttribute("announce", announce);
        return "announce-create";
    }

    @GetMapping("/announce-details")
    public String postDetails(){
        return "announce-details";
    }

}
