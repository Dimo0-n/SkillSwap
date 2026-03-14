package com.example.skillswap.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {

    @Bean
    public Cloudinary cloudinary(
            @Value("${CLOUDINARY_URL:}") String cloudinaryUrl,
            @Value("${CLOUDINARY_CLOUD_NAME:}") String cloudName,
            @Value("${CLOUDINARY_API_KEY:}") String apiKey,
            @Value("${CLOUDINARY_API_SECRET:}") String apiSecret
    ) {
        Cloudinary cloudinary = cloudinaryUrl != null && !cloudinaryUrl.isBlank()
                ? new Cloudinary(cloudinaryUrl)
                : new Cloudinary(ObjectUtils.asMap(
                        "cloud_name", requireValue("CLOUDINARY_CLOUD_NAME", cloudName),
                        "api_key", requireValue("CLOUDINARY_API_KEY", apiKey),
                        "api_secret", requireValue("CLOUDINARY_API_SECRET", apiSecret)
                ));

        cloudinary.config.secure = true;
        return cloudinary;
    }

    private String requireValue(String key, String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required Cloudinary configuration: " + key);
        }

        return value;
    }
}
