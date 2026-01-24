package com.example.skillswap.controller;

import com.example.skillswap.entity.Contact;
import com.example.skillswap.service.ContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
class ContactController {

    @Autowired
    private ContactService contactService;

    @GetMapping("/contact")
    public String contact(Model model){
        model.addAttribute("page", "contact" );
        model.addAttribute("contact", new Contact());
        return "contact";
    }

    @PostMapping("/contact/faq")
    public String contact(@ModelAttribute("contact") Contact contact){
        String fullName = contact.getFullName();
        String email = contact.getEmail();
        String message = contact.getMessage();
        contactService.saveContactMessage(fullName, email, message);
        return "redirect:/contact";
    }

}
