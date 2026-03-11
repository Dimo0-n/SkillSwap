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
            @Value("${CLAUDINARY_URL:}") String cloudinaryUrl,
            @Value("${CLAUDINARY_CLOUD_NAME:}") String cloudName,
            @Value("${CLAUDINARY_API_KEY:}") String apiKey,
            @Value("${CLAUDINARY_API_SECRET:}") String apiSecret
    ) {
        Cloudinary cloudinary = cloudinaryUrl != null && !cloudinaryUrl.isBlank()
                ? new Cloudinary(cloudinaryUrl)
                : new Cloudinary(ObjectUtils.asMap(
                        "cloud_name", requireValue("CLAUDINARY_CLOUD_NAME", cloudName),
                        "api_key", requireValue("CLAUDINARY_API_KEY", apiKey),
                        "api_secret", requireValue("CLAUDINARY_API_SECRET", apiSecret)
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
