package com.example.skillswap.service;

import com.example.skillswap.dto.ProfilDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ProfileService {

    void saveProfile(ProfilDto profilDto, MultipartFile profilePicture, String email) throws IOException;

    ProfilDto getProfileForView(String email);

    byte[] getProfileImageByEmail(String email) throws IOException;

    ProfilDto getAuthorByUserId(Long userId);
}
