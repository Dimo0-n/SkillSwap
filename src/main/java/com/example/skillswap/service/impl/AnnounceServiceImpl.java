package com.example.skillswap.service.impl;

import com.example.skillswap.dto.AnnounceDto;
import com.example.skillswap.entity.Announce;
import com.example.skillswap.entity.User;
import com.example.skillswap.repository.AnnounceRepository;
import com.example.skillswap.repository.UserRepository;
import com.example.skillswap.service.AnnounceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AnnounceServiceImpl implements AnnounceService {

    @Autowired
    private AnnounceRepository announceRepository;

    @Autowired
    private UserRepository userRepository;

    //5 anunturi care sunt afisate pentru pagina de index
    @Override
    public List<Announce> getLatest5Announces() {
        List<Announce> latest5Announces = announceRepository.findAll();
        return latest5Announces.subList(0, Math.min(5, latest5Announces.size()));
    }

    @Override
    public List<Announce> getAnnouncesList() {
        List<Announce> announceList = announceRepository.findAll();
        return announceList;
    }

    @Override
    public void save(AnnounceDto announceDto, Authentication auth) {

        User user = Optional.ofNullable(userRepository.findUserByEmail(auth.getName()))
                .orElseThrow(() -> new RuntimeException("User not found"));

        Announce announce = new Announce();

        announce.setTitle(announceDto.getTitle());
        announce.setDescription(announceDto.getDescription());
        announce.setAuthor(announceDto.getAuthor());
        announce.setCategoryOffered(announce.getCategoryOffered());
        announce.setCategoryRequired(announceDto.getCategoryRequired());
        announce.setImageKey(announceDto.getImageKey());
        announce.setImagePath(announceDto.getImagePath());
        announce.setAdditionalInfo(announceDto.getAdditionalInfo());
        announce.setDate(LocalDateTime.now());
        announce.setUser(user);

        announceRepository.save(announce);

    }


}

