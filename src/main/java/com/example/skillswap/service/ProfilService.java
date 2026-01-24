package com.example.skillswap.service;

import com.example.skillswap.entity.Profil;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ProfilService {

    void saveProfile(Profil profil, MultipartFile profilePicture, String email) throws IOException;

}
