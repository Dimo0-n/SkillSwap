package com.example.skillswap.service;

import com.example.skillswap.dto.AnnounceDto;
import com.example.skillswap.entity.Announce;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface AnnounceService {

    List<Announce> getLatest5Announces();

    List<Announce> getAnnouncesList();

    void save(AnnounceDto announceDto, Authentication auth);

//    List<Announce> getAnnouncesListByEmail(String email);

    List<Announce> getAnnouncesListByEmail(Long id);
}
