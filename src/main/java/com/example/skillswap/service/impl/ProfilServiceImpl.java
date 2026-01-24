package com.example.skillswap.service.impl;

import com.example.skillswap.entity.Profil;
import com.example.skillswap.entity.User;
import com.example.skillswap.repository.ProfilRepository;
import com.example.skillswap.repository.UserRepository;
import com.example.skillswap.service.ProfilService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class ProfilServiceImpl implements ProfilService {

    @Autowired
    private ProfilRepository profilRepository;

    @Autowired
    private UserRepository userRepository;

    public void saveProfile(Profil profil, MultipartFile profilePicture, String email) throws IOException {

        User user = userRepository.findUserByEmail(email);
        Profil profil1 = new Profil();

        if (profilePicture != null && !profilePicture.isEmpty()) {
            profil.setImage(profilePicture.getBytes());
        }
        profil.setUser(user);
        profil1 = profil;

        profilRepository.save(profil1);

    }













//    //pentru citire Availability
//public Set<Availability> getAvailability() {
//    Set<Availability> result = EnumSet.noneOf(Availability.class);
//
//    for (Availability a : Availability.values()) {
//        if ((availabilityMask & a.getBit()) != 0) {
//            result.add(a);
//        }
//    }
//    return result;
//}


}
