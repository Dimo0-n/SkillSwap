package com.example.skillswap.admin.config;

import com.example.skillswap.admin.entity.PlatformSetting;
import com.example.skillswap.admin.repository.PlatformSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AdminSettingsInitializer implements CommandLineRunner {

    private final PlatformSettingRepository platformSettingRepository;

    @Override
    public void run(String... args) {
        if (platformSettingRepository.count() > 0) {
            return;
        }

        platformSettingRepository.saveAll(List.of(
                setting("allowed.file.types", "Allowed file types", "Uploads", "jpg,jpeg,png,webp", "Comma-separated whitelist used by admin moderation."),
                setting("max.image.size.mb", "Maximum image size (MB)", "Uploads", "5", "Upper upload limit for profile and gallery images."),
                setting("session.duration.min", "Minimum session duration", "Sessions", "15", "Smallest allowed session duration in minutes."),
                setting("session.duration.max", "Maximum session duration", "Sessions", "180", "Largest allowed session duration in minutes."),
                setting("platform.rules", "Platform rules", "Content", "Be respectful. No harassment, scams, or explicit content.", "Rules text shown in moderation and help surfaces.")
        ));
    }

    private PlatformSetting setting(String key, String label, String category, String value, String description) {
        PlatformSetting setting = new PlatformSetting();
        setting.setSettingKey(key);
        setting.setLabel(label);
        setting.setCategory(category);
        setting.setSettingValue(value);
        setting.setDescription(description);
        return setting;
    }
}