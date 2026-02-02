package com.example.skillswap.controller;

import com.example.skillswap.dto.AnnounceDto;
import com.example.skillswap.entity.Announce;
import com.example.skillswap.service.AnnounceService;
import com.example.skillswap.service.impl.AnnounceImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
class AnnounceController {

    @Autowired
    private AnnounceService announceService;

    @Autowired
    private AnnounceImageService announceImageService;

    @GetMapping("/announces-list")
    public String categoryGrid(Model model){
        List<Announce> announcesList = announceService.getAnnouncesList();
        model.addAttribute("announcesList", announcesList);
        model.addAttribute("page", "announces-list" );
        return "announces-list";
    }

    @GetMapping("/announces/new")
    public String addAnnounce(Model model){
        AnnounceDto announce = new AnnounceDto();
        model.addAttribute("announce", announce);
        return "announce-create";
    }

    @PostMapping("/announce/save")
    public String saveAnnounce(@ModelAttribute("announce")AnnounceDto announceDto, Authentication auth){

        String safePath = announceImageService.safePath(
             announceDto.getCategoryOffered(),
             announceDto.getImageKey()
        );
        announceDto.setImagePath(safePath);

        announceService.save(announceDto, auth);

        return "redirect:/index";
    }

    @GetMapping("/announce-details")
    public String postDetails(){
        return "announce-details";
    }

}
