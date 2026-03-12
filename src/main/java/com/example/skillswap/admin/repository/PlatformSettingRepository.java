package com.example.skillswap.admin.repository;

import com.example.skillswap.admin.entity.PlatformSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlatformSettingRepository extends JpaRepository<PlatformSetting, String> {

    List<PlatformSetting> findAllByOrderByCategoryAscLabelAsc();
}