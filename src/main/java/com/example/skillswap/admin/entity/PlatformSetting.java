package com.example.skillswap.admin.entity;

import com.example.skillswap.entity.User;
import com.example.skillswap.util.UtcDateTimes;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "platform_setting")
@Getter
@Setter
@NoArgsConstructor
public class PlatformSetting {

    @Id
    @Column(nullable = false, length = 80)
    private String settingKey;

    @Column(nullable = false, length = 120)
    private String label;

    @Column(nullable = false, length = 40)
    private String category;

    @Column(nullable = false, length = 2000)
    private String settingValue;

    @Column(length = 255)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by_id")
    private User updatedBy;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    void touch() {
        updatedAt = UtcDateTimes.now();
    }
}