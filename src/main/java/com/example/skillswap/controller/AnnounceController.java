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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;

import java.util.List;

@Controller
class AnnounceController {

    @Autowired
    private AnnounceService announceService;

    @Autowired
    private AnnounceImageService announceImageService;

    @GetMapping("/announces-list")
    public String categoryGrid(Model model) {
        List<Announce> announcesList = announceService.getAnnouncesList();
        model.addAttribute("announcesList", announcesList);
        model.addAttribute("page", "announces-list");
        return "announces-list";
    }

    @GetMapping("/announces/new")
    public String addAnnounce(Model model) {
        AnnounceDto announce = new AnnounceDto();
        model.addAttribute("announce", announce);
        return "announce-create";
    }

    @PostMapping("/announce/save")
    public String saveAnnounce(@Valid @ModelAttribute("announce") AnnounceDto announceDto, BindingResult bindingResult,
            Authentication auth, Model model) {

        if (bindingResult.hasErrors()) {
            return "announce-create";
        }

        String safePath = announceImageService.safePath(
                announceDto.getCategoryOffered(),
                announceDto.getImageKey());
        announceDto.setImagePath(safePath);

        announceService.save(announceDto, auth);

        return "redirect:/index";
    }

    @GetMapping("/announce-details/{id}")
    public String postDetails(@PathVariable Long id, Model model) {

        AnnounceDto announceDto = announceService.getAnnounceById(id);

        model.addAttribute("announce", announceDto);

        return "announce-details";
    }

}
