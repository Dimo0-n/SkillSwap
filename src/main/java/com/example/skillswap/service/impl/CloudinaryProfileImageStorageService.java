package com.example.skillswap.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.skillswap.service.ProfileImageStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryProfileImageStorageService implements ProfileImageStorageService {

    private static final String PROFILE_IMAGE_PUBLIC_ID_PREFIX = "skillswap/profile-images/user-";

    private final Cloudinary cloudinary;

    @Override
    public String uploadProfileImage(MultipartFile profileImage, Long userId) throws IOException {
        if (profileImage == null || profileImage.isEmpty()) {
            throw new IllegalArgumentException("Profile image file is missing.");
        }

        String contentType = profileImage.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed for profile uploads.");
        }

        Map<?, ?> uploadResult = cloudinary.uploader().upload(
                profileImage.getBytes(),
                ObjectUtils.asMap(
                        "public_id", PROFILE_IMAGE_PUBLIC_ID_PREFIX + userId,
                        "overwrite", true,
                        "invalidate", true,
                        "resource_type", "image"
                )
        );

        Object secureUrl = uploadResult.get("secure_url");
        if (!(secureUrl instanceof String url) || url.isBlank()) {
            throw new IOException("Cloudinary did not return a secure URL for the uploaded profile image.");
        }

        return url;
    }
}
