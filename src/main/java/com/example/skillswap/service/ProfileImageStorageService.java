package com.example.skillswap.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ProfileImageStorageService {

    String uploadProfileImage(MultipartFile profileImage, Long userId) throws IOException;
}
