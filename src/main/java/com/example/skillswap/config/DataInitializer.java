package com.example.skillswap.config;

import com.example.skillswap.entity.Category;
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
import java.util.List;

@Component
public class DataInitializer {

    @Autowired
    private AnnounceRepository announceRepository;

    @Autowired
    private CategoryRepository categoryRepository;

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

}
