package com.example.skillswap.config;

import com.example.skillswap.entity.Announce;
import com.example.skillswap.entity.AnnounceImage;
import com.example.skillswap.entity.Category;
import com.example.skillswap.repository.AnnounceImagesRepository;
import com.example.skillswap.repository.AnnounceRepository;
import com.example.skillswap.repository.CategoryRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataInitializer {

    @Autowired
    private AnnounceRepository announceRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private AnnounceImagesRepository announceImagesRepository;


    public DataInitializer(AnnounceRepository announceRepository) {
        this.announceRepository = announceRepository;
    }

    //pentru anunturi
    private byte[] loadImage(String imagePath, int width, int height) {
        try {
            // Încarcă imaginea din calea specificată
            ClassPathResource resource = new ClassPathResource(imagePath);
            InputStream inputStream = resource.getInputStream();

            // Citește imaginea ca BufferedImage
            BufferedImage originalImage = ImageIO.read(inputStream);

            // Redimensionează imaginea la dimensiunea dorită
            BufferedImage resizedImage = new BufferedImage(width, height, originalImage.getType());
            Graphics2D g = resizedImage.createGraphics();
            g.drawImage(originalImage, 0, 0, width, height, null);
            g.dispose();

            // Convertește imaginea redimensionată într-un array de bytes
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(resizedImage, "jpg", baos);
            return baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }

    @PostConstruct
    public void init() throws Exception {

        if (categoryRepository.count() == 0) {
            addCategories();
        }

        if (announceImagesRepository.count() == 0) {
            addAnnouncesImages();
        }

    }

    private void addCategories() throws Exception {
        List<Category> categoryList = List.of(
                new Category(1L, "Programare"),
                new Category(2L, "Design"),
                new Category(3L, "Fotografie"),
                new Category(4L, "Scriere"),
                new Category(5L, "Marketing"),
                new Category(6L, "Limbi străine"),
                new Category(7L, "Business"),
                new Category(8L, " Coaching"),
                new Category(9L, "DIY"),
                new Category(10L, "Artă"),
                new Category(11L, "Muzică")
        );
        categoryRepository.saveAll(categoryList);
    }

    private void addAnnouncesImages() {
        List<AnnounceImage> announceImageList = List.of(
                new AnnounceImage(0, "default", "img/skill/skill-default.png"),
                new AnnounceImage(0, "cooking", "img/skill/skill-cooking.png"),
                new AnnounceImage(0, "dance", "img/skill/skill-dance.png"),
                new AnnounceImage(0, "english", "img/skill/skill-english.png"),
                new AnnounceImage(0, "guitar", "img/skill/skill-guitar.png"),
                new AnnounceImage(0, "photography", "img/skill/skill-photography.png"),
                new AnnounceImage(0, "photoshop", "img/skill/skill-photoshop.png"),
                new AnnounceImage(0, "programming", "img/skill/skill-programming.png"),
                new AnnounceImage(0, "public speaking", "img/skill/skill-public-speaking.png")
        );
        announceImagesRepository.saveAll(announceImageList);
    }

}
