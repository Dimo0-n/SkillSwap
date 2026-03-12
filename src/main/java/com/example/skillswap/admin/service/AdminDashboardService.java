package com.example.skillswap.admin.service;

import com.example.skillswap.admin.dto.AdminDashboardOverviewDto;
import com.example.skillswap.admin.dto.AdminImageRowDto;
import com.example.skillswap.admin.dto.AdminNotificationRowDto;
import com.example.skillswap.admin.dto.AdminPostRowDto;
import com.example.skillswap.admin.dto.AdminReportRowDto;
import com.example.skillswap.admin.dto.AdminReviewRowDto;
import com.example.skillswap.admin.dto.AdminSettingRowDto;
import com.example.skillswap.admin.dto.AdminSkillRowDto;
import com.example.skillswap.admin.dto.AdminSkillImageGroupDto;
import com.example.skillswap.admin.dto.AdminStatisticsDto;
import com.example.skillswap.admin.dto.AdminUserRowDto;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface AdminDashboardService {

    AdminDashboardOverviewDto getOverview();

    Page<AdminUserRowDto> getUsers(String query, String status, int page, int size);

    void suspendUser(Long targetUserId, Long adminUserId);

    void banUser(Long targetUserId, Long adminUserId);

    void softDeleteUser(Long targetUserId, Long adminUserId);

    String resetPassword(Long targetUserId, Long adminUserId);

    List<AdminSkillRowDto> getSkills(String query);

    void mergeSkills(String targetName, List<String> sourceNames, Long adminUserId);

    void deleteSkill(String skillName, Long adminUserId);

    Page<AdminPostRowDto> getPosts(String query, boolean spamOnly, int page, int size);

    void deletePost(Long postId, Long adminUserId);

    void markPostAsSpam(Long postId, Long adminUserId);

    Page<AdminReviewRowDto> getReviews(String query, Integer rating, boolean reportedOnly, int page, int size);

    void deleteReview(Long reviewId, Long adminUserId);

    Page<AdminReportRowDto> getReports(String status, int page, int size);

    void resolveReport(Long reportId, String action, Long adminUserId);

    AdminStatisticsDto getStatistics();

    List<AdminNotificationRowDto> getRecentNotifications();

    int sendPlatformAnnouncement(String title, String message, String targetUrl, Long adminUserId);

    List<AdminImageRowDto> getImages(boolean suspiciousOnly);

    List<AdminSkillImageGroupDto> getSkillImageGroups();

    void deleteImage(String imageType, Long entityId, Long adminUserId);

    List<AdminSettingRowDto> getSettings();

    void updateSettings(Map<String, String> valuesByKey, Long adminUserId);
}