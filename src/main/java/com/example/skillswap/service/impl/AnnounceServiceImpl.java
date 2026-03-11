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

    //lista cu toate anunturile care sunt afisate din DB
    @Override
    public List<Announce> getAnnouncesList() {
        return announceRepository.findAllByOrderByIdDesc();
    }

    //Crearea unui anunt nou de catre user-ul logat
    @Override
    public void save(AnnounceDto announceDto, Authentication auth) {

        User user = Optional.ofNullable(userRepository.findUserByEmail(auth.getName()))
                .orElseThrow(() -> new RuntimeException("User not found"));

        Announce announce = new Announce();

        announce.setTitle(normalizeNullableText(announceDto.getTitle()));
        announce.setDescription(normalizeNullableText(announceDto.getDescription()));
        announce.setAuthor(normalizeNullableText(announceDto.getAuthor()));
        announce.setCategoryOffered(normalizeNullableText(announceDto.getCategoryOffered()));
        announce.setCategoryRequired(normalizeNullableText(announceDto.getCategoryRequired()));
        announce.setImageKey(announceDto.getImageKey());
        announce.setImagePath(announceDto.getImagePath());
        announce.setAdditionalInfo(normalizeNullableText(announceDto.getAdditionalInfo()));
        announce.setDate(LocalDateTime.now());
        announce.setUser(user);

        announceRepository.save(announce);
    }

    //afisarea anunturilor pe care le-a publicat user-ul
    @Override
    public List<Announce> getAnnouncesListByEmail(Long id) {
        return announceRepository.getAnnouncesListByEmail(id);
    }

    @Override
    public void deleteAnnounceById(Long id) {
        announceRepository.deleteById(id);
    }

    @Override
    public AnnounceDto getAnnounceById(Long id) {

        Optional<Announce> announce = announceRepository.findById(id);
        AnnounceDto announceDto = new AnnounceDto();

        announceDto.setId(announce.get().getId());
        announceDto.setTitle(announce.get().getTitle());
        announceDto.setDescription(announce.get().getDescription());
        announceDto.setAuthor(announce.get().getAuthor());
        announceDto.setCategoryOffered(announce.get().getCategoryOffered());
        announceDto.setCategoryRequired(announce.get().getCategoryRequired());
        announceDto.setImageKey(announce.get().getImageKey());
        announceDto.setImagePath(announce.get().getImagePath());
        announceDto.setAdditionalInfo(announce.get().getAdditionalInfo());
        announceDto.setDate(announce.get().getDate());
        announceDto.setUserId(announce.get().getUser().getId());

        return announceDto;
    }

    private String normalizeNullableText(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }


}

