package com.example.skillswap.service;

import com.example.skillswap.dto.ProfilDto;
import com.example.skillswap.entity.Profil;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ProfileService {

    void saveProfile(Profil profil, MultipartFile profilePicture, String email) throws IOException;

    ProfilDto getProfileForView(String email);

    byte[] getProfileImageByEmail(String email) throws IOException;
}
