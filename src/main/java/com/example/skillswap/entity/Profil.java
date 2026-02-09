package com.example.skillswap.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Arrays;
import java.util.Base64;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Profil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String profession;

    private String bioShort;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String completeDescription;

    private int availabilityMask;

    private String limits;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String competences;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String strengths;

    @Lob
    @Column(name = "image",  columnDefinition = "LONGBLOB")
    private byte[] image;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public String getBase64Image() {
        if (this.image != null) {
            return Base64.getEncoder().encodeToString(this.image);
        }
        return null;
    }

    public void setBase64Image(byte[] base64Image) {
        if (base64Image != null) {
            this.image = Base64.getDecoder().decode(base64Image);
        } else {
            this.image = null;
        }
    }

}

