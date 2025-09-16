package com.example.skillswap.sevice;

import com.example.skillswap.entity.Contact;
import com.example.skillswap.repository.ContactRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ContactServiceImpl implements ContactService {

    @Autowired
    private ContactRepository contactRepository;

    @Override
    public void saveContactMessage(String fullName, String email, String message) {
        Contact contact = new Contact();

        contact.setFullName(fullName);
        contact.setEmail(email);
        contact.setMessage(message);
        contact.setSendDate(LocalDateTime.now());

        contactRepository.save(contact);
    }
}
