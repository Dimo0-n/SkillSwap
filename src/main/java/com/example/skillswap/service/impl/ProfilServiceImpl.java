package com.example.skillswap.service.impl;

import com.example.skillswap.dto.ProfilDto;
import com.example.skillswap.entity.Profil;
import com.example.skillswap.entity.User;
import com.example.skillswap.repository.ProfilRepository;
import com.example.skillswap.repository.UserRepository;
import com.example.skillswap.service.ProfilService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import org.springframework.core.io.ClassPathResource;

@Service
public class ProfilServiceImpl implements ProfilService {

    @Autowired
    private ProfilRepository profilRepository;

    @Autowired
    private UserRepository userRepository;

    public void saveProfile(Profil profil, MultipartFile profilePicture, String email) throws IOException {

        User user = Optional.ofNullable(userRepository.findUserByEmail(email))
                .orElseThrow(() -> new RuntimeException("User not found"));

        Profil toSave = profilRepository.findFirstByUserEmailOrderByIdDesc(email)
                .orElseGet(Profil::new);

        toSave.setUser(user);
        toSave.setName(profil.getName());
        toSave.setProfession(profil.getProfession());
        toSave.setBioShort(profil.getBioShort());
        toSave.setCompleteDescription(profil.getCompleteDescription());
        toSave.setAvailabilityMask(profil.getAvailabilityMask());
        toSave.setLimits(profil.getLimits());
        toSave.setCompetences(profil.getCompetences());
        toSave.setStrengths(profil.getStrengths());

        if (profilePicture != null && !profilePicture.isEmpty()) {
            toSave.setImage(profilePicture.getBytes());
        }

        profilRepository.save(toSave);

    }

    @Override
    public ProfilDto getProfileForView(String username) {

        Profil profil = profilRepository
                .findFirstByUserEmailOrderByIdDesc(username)
                .orElseThrow(() -> new RuntimeException("Profil not found"));

        ProfilDto dto = new ProfilDto();

        dto.setName(profil.getName());
        dto.setProfession(profil.getProfession());
        dto.setBioShort(profil.getBioShort());
        dto.setCompleteDescription(profil.getCompleteDescription());

        dto.setCompetences(split(profil.getCompetences()));
        dto.setStrengths(split(profil.getStrengths()));
        dto.setLimits(split(profil.getLimits()));

        dto.setImageUrl("/profile/image/" + username);

        return dto;
    }

    private List<String> split(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return List.of(value.split(","));
    }


    public byte[] getProfileImageByEmail(String email) throws IOException {
        Profil profil = profilRepository
                .findFirstByUserEmailOrderByIdDesc(email)
                .orElseThrow(() -> new RuntimeException("Profil not found"));

        if (profil.getImage() == null) {
            ClassPathResource resource = new ClassPathResource("static/img/default-avatar.png");
            try (InputStream is = resource.getInputStream()) {
                return is.readAllBytes();
            }
        }
        return profil.getImage();
    }



//    //pentru citire Availability
//public Set<Availability> getAvailability() {
//    Set<Availability> result = EnumSet.noneOf(Availability.class);
//
//    for (Availability a : Availability.values()) {
//        if ((availability & a.getBit()) != 0) {
//            result.add(a);
//        }
//    }
//    return result;
//}


}
