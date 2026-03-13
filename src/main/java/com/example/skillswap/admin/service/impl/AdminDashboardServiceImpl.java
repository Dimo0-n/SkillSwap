package com.example.skillswap.admin.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.skillswap.admin.dto.AdminChartPointDto;
import com.example.skillswap.admin.dto.AdminDashboardOverviewDto;
import com.example.skillswap.admin.dto.AdminImageRowDto;
import com.example.skillswap.admin.dto.AdminNotificationRowDto;
import com.example.skillswap.admin.dto.AdminPostRowDto;
import com.example.skillswap.admin.dto.AdminRecentActionDto;
import com.example.skillswap.admin.dto.AdminReportRowDto;
import com.example.skillswap.admin.dto.AdminReviewRowDto;
import com.example.skillswap.admin.dto.AdminSettingRowDto;
import com.example.skillswap.admin.dto.AdminSkillImageGroupDto;
import com.example.skillswap.admin.dto.AdminSkillRowDto;
import com.example.skillswap.admin.dto.AdminStatisticsDto;
import com.example.skillswap.admin.dto.AdminSummaryMetricDto;
import com.example.skillswap.admin.dto.AdminUserRowDto;
import com.example.skillswap.admin.entity.AdminAuditLog;
import com.example.skillswap.admin.entity.ModerationReport;
import com.example.skillswap.admin.entity.PlatformSetting;
import com.example.skillswap.admin.enums.AdminActionType;
import com.example.skillswap.admin.enums.ModerationReportStatus;
import com.example.skillswap.admin.enums.ReportTargetType;
import com.example.skillswap.admin.repository.AdminAuditLogRepository;
import com.example.skillswap.admin.repository.ModerationReportRepository;
import com.example.skillswap.admin.repository.PlatformSettingRepository;
import com.example.skillswap.admin.service.AdminDashboardService;
import com.example.skillswap.entity.Announce;
import com.example.skillswap.entity.Notification;
import com.example.skillswap.entity.ProfileComment;
import com.example.skillswap.entity.Profil;
import com.example.skillswap.entity.Role;
import com.example.skillswap.entity.SkillSwapProposal;
import com.example.skillswap.entity.User;
import com.example.skillswap.enums.NotificationType;
import com.example.skillswap.enums.SkillSwapProposalStatus;
import com.example.skillswap.repository.AnnounceRepository;
import com.example.skillswap.repository.NotificationRepository;
import com.example.skillswap.repository.ProfileCommentRepository;
import com.example.skillswap.repository.ProfileRepository;
import com.example.skillswap.repository.RoleRepository;
import com.example.skillswap.repository.SkillSwapProposalRepository;
import com.example.skillswap.repository.UserRepository;
import com.example.skillswap.service.AnnounceImageService;
import com.example.skillswap.service.NotificationService;
import com.example.skillswap.util.UtcDateTimes;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final String PROFILE_IMAGE_PUBLIC_ID_PREFIX = "skillswap/profile-images/user-";
    private static final long ROLE_ADMIN_ID = 1L;
    private static final long ROLE_USER_ID = 2L;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AnnounceRepository announceRepository;
    private final SkillSwapProposalRepository skillSwapProposalRepository;
    private final ProfileRepository profileRepository;
    private final ProfileCommentRepository profileCommentRepository;
    private final NotificationRepository notificationRepository;
    private final ModerationReportRepository moderationReportRepository;
    private final PlatformSettingRepository platformSettingRepository;
    private final AdminAuditLogRepository adminAuditLogRepository;
    private final NotificationService notificationService;
    private final AnnounceImageService announceImageService;
    private final PasswordEncoder passwordEncoder;
    private final Cloudinary cloudinary;

    @Override
    @Transactional(readOnly = true)
    public AdminDashboardOverviewDto getOverview() {
        List<User> users = activeUsers();
        List<SkillSwapProposal> proposals = skillSwapProposalRepository.findAll();
        List<ModerationReport> reports = moderationReportRepository.findAll();
        List<ProfileComment> reviews = profileCommentRepository.findAll();

        long activeToday = users.stream()
                .filter(user -> isSameDay(user.getLastSeenAt(), LocalDate.now()))
                .count();

        List<AdminSummaryMetricDto> metrics = List.of(
                metric("Total Users", users.size(), "Registered accounts", "primary"),
                metric("Active Users Today", activeToday, "Seen in the last 24h", "success"),
                metric("Total Skills", buildSkillInventory().size(), "Distinct skill labels in use", "accent"),
                metric("Total Sessions Created", proposals.size(), "Skill swap proposals", "info"),
                metric("Total Reviews", reviews.size(), "Profile reviews", "warning"),
                metric("Reports Pending", moderationReportRepository.countByStatus(ModerationReportStatus.PENDING), "Need moderation", "danger")
        );

        List<AdminReportRowDto> pendingReports = reports.stream()
                .filter(report -> report.getStatus() == ModerationReportStatus.PENDING)
                .sorted(Comparator.comparing(ModerationReport::getCreatedAt).reversed())
                .limit(6)
                .map(this::toReportRow)
                .toList();

        return new AdminDashboardOverviewDto(
                metrics,
                buildUserRegistrationSeries(users, 7),
                buildSessionsByWeekSeries(proposals, 8),
                buildPopularSkillsSeries(6),
                adminAuditLogRepository.findTop12ByOrderByCreatedAtDesc().stream().map(this::toRecentAction).toList(),
                pendingReports
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminUserRowDto> getUsers(String query, String status, int page, int size) {
        List<SkillSwapProposal> proposals = skillSwapProposalRepository.findAll();
        Map<Long, Long> completedSessionsByUser = proposals.stream()
                .filter(proposal -> proposal.getStatus() == SkillSwapProposalStatus.ACCEPTED)
                .flatMap(proposal -> Stream.of(proposal.getOwner().getId(), proposal.getRequester().getId()))
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        Map<Long, Double> reputationByUser = profileRepository.findAll().stream()
            .filter(profile -> profile.getUser() != null)
            .filter(profile -> profile.getReputationScore() != null)
                .collect(Collectors.toMap(profile -> profile.getUser().getId(), Profil::getReputationScore, (left, right) -> right));

        String needle = normalizeQuery(query);
        List<AdminUserRowDto> rows = activeUsers().stream()
                .filter(user -> matchesUser(user, needle))
                .filter(user -> matchesUserStatus(user, status))
                .sorted(Comparator.comparing(User::getRegisterData, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(user -> new AdminUserRowDto(
                        user.getId(),
                        user.getFullName(),
                        user.getEmail(),
                        user.getRoles().stream().map(role -> role.getName().replace("ROLE_", "")).sorted().collect(Collectors.joining(", ")),
                        user.getRegisterData(),
                        user.getLastLoginAt(),
                        completedSessionsByUser.getOrDefault(user.getId(), 0L),
                        reputationByUser.get(user.getId()),
                        user.isSuspended(),
                        user.isBanned(),
                        user.isDeleted()
                ))
                .toList();

        return paginate(rows, page, size);
    }

    @Override
    @Transactional
    public void suspendUser(Long targetUserId, Long adminUserId) {
        User user = getManagedUser(targetUserId);
        user.setSuspended(true);
        user.setOnline(false);
        logAction(adminUserId, AdminActionType.USER_SUSPENDED, "USER", user.getId(), user.getEmail(), "Account suspended.");
    }

    @Override
    @Transactional
    public void banUser(Long targetUserId, Long adminUserId) {
        User user = getManagedUser(targetUserId);
        user.setBanned(true);
        user.setSuspended(false);
        user.setOnline(false);
        logAction(adminUserId, AdminActionType.USER_BANNED, "USER", user.getId(), user.getEmail(), "Account banned.");
    }

    @Override
    @Transactional
    public void grantAdminRole(Long targetUserId, Long adminUserId) {
        User user = getManagedUser(targetUserId);

        Role adminRole = roleRepository.findById(ROLE_ADMIN_ID)
                .or(() -> roleRepository.findByName("ROLE_ADMIN"))
                .orElseThrow(() -> new IllegalArgumentException("Admin role not found."));
        Role userRole = roleRepository.findById(ROLE_USER_ID)
                .or(() -> roleRepository.findByName("ROLE_USER"))
                .orElseThrow(() -> new IllegalArgumentException("User role not found."));

        user.getRoles().add(userRole);
        user.getRoles().add(adminRole);

        logAction(adminUserId, AdminActionType.USER_ROLE_ADMIN_GRANTED, "USER", user.getId(), user.getEmail(), "Admin role assigned.");
    }

    @Override
    @Transactional
    public void softDeleteUser(Long targetUserId, Long adminUserId) {
        User user = getManagedUser(targetUserId);
        user.setDeleted(true);
        user.setOnline(false);
        user.setSuspended(false);
        user.setBanned(false);
        logAction(adminUserId, AdminActionType.USER_DELETED, "USER", user.getId(), user.getEmail(), "Soft deleted account.");
    }

    @Override
    @Transactional
    public String resetPassword(Long targetUserId, Long adminUserId) {
        User user = getManagedUser(targetUserId);
        String temporaryPassword = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        user.setPassword(passwordEncoder.encode(temporaryPassword));
        logAction(adminUserId, AdminActionType.USER_PASSWORD_RESET, "USER", user.getId(), user.getEmail(), "Temporary password issued.");
        return temporaryPassword;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminSkillRowDto> getSkills(String query) {
        String needle = normalizeQuery(query);
        return buildSkillInventory().values().stream()
                .filter(skill -> needle.isBlank() || skill.canonicalName().toLowerCase(Locale.ROOT).contains(needle)
                        || skill.variants().stream().anyMatch(variant -> variant.toLowerCase(Locale.ROOT).contains(needle)))
                .sorted(Comparator.comparing(AdminSkillAggregate::occurrences).reversed()
                        .thenComparing(AdminSkillAggregate::canonicalName))
                .map(skill -> new AdminSkillRowDto(skill.canonicalName(), skill.occurrences(), String.join(", ", skill.variants())))
                .toList();
    }

    @Override
    @Transactional
    public void mergeSkills(String targetName, List<String> sourceNames, Long adminUserId) {
        String canonicalTarget = normalizeSkill(targetName);
        if (canonicalTarget.isBlank()) {
            throw new IllegalArgumentException("Target skill name is required.");
        }

        Set<String> normalizedSources = sourceNames == null ? Set.of() : sourceNames.stream()
                .map(this::normalizeSkill)
                .filter(source -> !source.isBlank() && !source.equals(canonicalTarget))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (normalizedSources.isEmpty()) {
            return;
        }

        announceRepository.findAll().forEach(announce -> {
            announce.setCategoryOffered(rewriteSkill(announce.getCategoryOffered(), targetName, normalizedSources));
            announce.setCategoryRequired(rewriteSkill(announce.getCategoryRequired(), targetName, normalizedSources));
        });

        skillSwapProposalRepository.findAll().forEach(proposal -> {
            proposal.setOfferedSkill(rewriteSkill(proposal.getOfferedSkill(), targetName, normalizedSources));
            proposal.setRequestedSkill(rewriteSkill(proposal.getRequestedSkill(), targetName, normalizedSources));
        });

        logAction(adminUserId, AdminActionType.SKILL_MERGED, "SKILL", null, targetName,
                "Merged: " + String.join(", ", normalizedSources));
    }

    @Override
    @Transactional
    public void deleteSkill(String skillName, Long adminUserId) {
        String normalized = normalizeSkill(skillName);
        announceRepository.findAll().forEach(announce -> {
            if (normalizeSkill(announce.getCategoryOffered()).equals(normalized)) {
                announce.setCategoryOffered("");
            }
            if (normalizeSkill(announce.getCategoryRequired()).equals(normalized)) {
                announce.setCategoryRequired("");
            }
        });

        skillSwapProposalRepository.findAll().forEach(proposal -> {
            if (normalizeSkill(proposal.getOfferedSkill()).equals(normalized)) {
                proposal.setOfferedSkill("");
            }
            if (normalizeSkill(proposal.getRequestedSkill()).equals(normalized)) {
                proposal.setRequestedSkill("");
            }
        });

        logAction(adminUserId, AdminActionType.SKILL_DELETED, "SKILL", null, skillName, "Removed skill references.");
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminPostRowDto> getPosts(String query, boolean spamOnly, int page, int size) {
        String needle = normalizeQuery(query);
        Map<Long, Long> proposalCountByAnnounce = skillSwapProposalRepository.findAll().stream()
                .collect(Collectors.groupingBy(proposal -> proposal.getAnnounce().getId(), Collectors.counting()));

        List<AdminPostRowDto> rows = announceRepository.findAllByOrderByIdDesc().stream()
            .filter(announce -> !announce.isDeletedByAdmin())
                .filter(announce -> !spamOnly || announce.isMarkedAsSpam())
                .filter(announce -> matchesPost(announce, needle))
                .map(announce -> new AdminPostRowDto(
                        announce.getId(),
                        announce.getTitle(),
                        announce.getAuthor(),
                        announce.getUser() != null ? announce.getUser().getEmail() : "Unknown",
                        blankFallback(announce.getCategoryOffered()),
                        blankFallback(announce.getCategoryRequired()),
                        announce.getDate(),
                        announce.isMarkedAsSpam(),
                        proposalCountByAnnounce.getOrDefault(announce.getId(), 0L)
                ))
                .toList();

        return paginate(rows, page, size);
    }

    @Override
    @Transactional
    public void deletePost(Long postId, Long adminUserId) {
        Announce announce = announceRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found."));
        announce.setDeletedByAdmin(true);
        announce.setMarkedAsSpam(true);
        announce.setModeratedAt(UtcDateTimes.now());
        logAction(adminUserId, AdminActionType.POST_DELETED, "POST", postId, announce.getTitle(), "Post hidden from the platform by admin.");
    }

    @Override
    @Transactional
    public void markPostAsSpam(Long postId, Long adminUserId) {
        Announce announce = announceRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found."));
        announce.setMarkedAsSpam(true);
        announce.setModeratedAt(UtcDateTimes.now());
        logAction(adminUserId, AdminActionType.POST_MARKED_SPAM, "POST", postId, announce.getTitle(), "Marked as spam.");
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminReviewRowDto> getReviews(String query, Integer rating, boolean reportedOnly, int page, int size) {
        String needle = normalizeQuery(query);
        List<AdminReviewRowDto> rows = profileCommentRepository.findAll().stream()
                .filter(comment -> !reportedOnly || comment.isReported())
                .filter(comment -> rating == null || Objects.equals(comment.getRating(), rating))
                .filter(comment -> matchesReview(comment, needle))
                .sorted(Comparator.comparing(ProfileComment::getCreatedAt).reversed())
                .map(comment -> new AdminReviewRowDto(
                        comment.getId(),
                        comment.getProfileOwner().getFullName(),
                        comment.getAuthorDisplayName(),
                        comment.getContent(),
                        comment.getRating(),
                        comment.getCreatedAt(),
                        comment.isReported()
                ))
                .toList();

        return paginate(rows, page, size);
    }

    @Override
    @Transactional
    public void deleteReview(Long reviewId, Long adminUserId) {
        ProfileComment review = profileCommentRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found."));
        profileCommentRepository.delete(review);
        logAction(adminUserId, AdminActionType.REVIEW_DELETED, "REVIEW", reviewId, review.getAuthorDisplayName(), "Abusive review deleted.");
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminReportRowDto> getReports(String status, int page, int size) {
        List<AdminReportRowDto> rows = moderationReportRepository.findAll().stream()
                .filter(report -> status == null || status.isBlank() || report.getStatus().name().equalsIgnoreCase(status))
                .sorted(Comparator.comparing(ModerationReport::getCreatedAt).reversed())
                .map(this::toReportRow)
                .toList();

        return paginate(rows, page, size);
    }

    @Override
    @Transactional
    public void resolveReport(Long reportId, String action, Long adminUserId) {
        ModerationReport report = moderationReportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found."));
        User admin = userRepository.findById(adminUserId)
                .orElseThrow(() -> new IllegalArgumentException("Admin not found."));

        switch (normalizeQuery(action)) {
            case "ignore" -> {
                report.setStatus(ModerationReportStatus.IGNORED);
                logAction(adminUserId, AdminActionType.REPORT_IGNORED, report.getTargetType().name(), report.getTargetId(), report.getTargetLabel(), report.getReason());
            }
            case "warn" -> {
                warnTargetUser(report);
                report.setStatus(ModerationReportStatus.WARNED);
                logAction(adminUserId, AdminActionType.REPORT_WARNED, report.getTargetType().name(), report.getTargetId(), report.getTargetLabel(), report.getReason());
            }
            case "delete-content" -> {
                deleteReportedContent(report, adminUserId);
                report.setStatus(ModerationReportStatus.CONTENT_DELETED);
                logAction(adminUserId, AdminActionType.REPORT_CONTENT_DELETED, report.getTargetType().name(), report.getTargetId(), report.getTargetLabel(), report.getReason());
            }
            case "ban-user" -> {
                User targetUser = resolveTargetUser(report);
                targetUser.setBanned(true);
                targetUser.setSuspended(false);
                targetUser.setOnline(false);
                report.setStatus(ModerationReportStatus.USER_BANNED);
                logAction(adminUserId, AdminActionType.REPORT_USER_BANNED, "USER", targetUser.getId(), targetUser.getEmail(), report.getReason());
            }
            default -> throw new IllegalArgumentException("Unsupported report action.");
        }

        report.setResolvedAt(UtcDateTimes.now());
        report.setResolvedBy(admin);
        report.setResolutionNotes(action);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminStatisticsDto getStatistics() {
        List<User> users = activeUsers();
        List<SkillSwapProposal> proposals = skillSwapProposalRepository.findAll();

        long activeSessions = proposals.stream()
                .filter(proposal -> proposal.getStatus() == SkillSwapProposalStatus.PENDING || proposal.getStatus() == SkillSwapProposalStatus.NEGOTIATING)
                .count();
        long completedSessions = proposals.stream()
                .filter(proposal -> proposal.getStatus() == SkillSwapProposalStatus.ACCEPTED)
                .count();

        List<AdminSummaryMetricDto> metrics = List.of(
                metric("User Growth", users.size(), "Total registered accounts", "primary"),
                metric("Popular Skills", buildSkillInventory().size(), "Unique skill labels monitored", "accent"),
                metric("Active Sessions", activeSessions, "Pending or negotiating", "info"),
                metric("Completed Sessions", completedSessions, "Accepted skill swaps", "success")
        );

        return new AdminStatisticsDto(
                metrics,
                buildUserRegistrationSeries(users, 14),
                buildProposalStatusSeries(proposals, List.of(SkillSwapProposalStatus.PENDING, SkillSwapProposalStatus.NEGOTIATING)),
                buildProposalStatusSeries(proposals, List.of(SkillSwapProposalStatus.ACCEPTED)),
                buildPopularSkillsSeries(8)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminNotificationRowDto> getRecentNotifications() {
        return notificationRepository.findTop12ByTypeOrderByCreatedAtDesc(NotificationType.SYSTEM).stream()
                .map(notification -> new AdminNotificationRowDto(
                        notification.getId(),
                        notification.getTitle(),
                        notification.getMessage(),
                        notification.getTargetUrl(),
                        notification.getCreatedAt()
                ))
                .toList();
    }

    @Override
    @Transactional
    public int sendPlatformAnnouncement(String title, String message, String targetUrl, Long adminUserId) {
        int recipients = notificationService.broadcastSystemNotification(title, message, targetUrl);
        logAction(adminUserId, AdminActionType.NOTIFICATION_SENT, "NOTIFICATION", null, title, "Broadcast sent to " + recipients + " users.");
        return recipients;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminImageRowDto> getImages(boolean suspiciousOnly) {
        List<AdminImageRowDto> rows = new ArrayList<>();

        profileRepository.findAll().stream()
                .filter(profile -> profile.getImageUrl() != null && !profile.getImageUrl().isBlank())
                .map(profile -> new AdminImageRowDto(
                        "PROFILE",
                        profile.getId(),
                        profile.getUser() != null ? profile.getUser().getFullName() : "Unknown",
                        "Profile photo",
                        profile.getImageUrl(),
                        PROFILE_IMAGE_PUBLIC_ID_PREFIX + (profile.getUser() != null ? profile.getUser().getId() : profile.getId()),
                        isSuspiciousImage(profile.getImageUrl())
                ))
                .forEach(rows::add);

        announceRepository.findAll().stream()
            .filter(announce -> !announce.isDeletedByAdmin())
                .filter(announce -> announce.getImagePath() != null && !announce.getImagePath().isBlank())
                .map(announce -> new AdminImageRowDto(
                        "POST",
                        announce.getId(),
                        announce.getAuthor(),
                        announce.getTitle(),
                        announce.getImagePath(),
                        announce.getImageKey(),
                        isSuspiciousImage(announce.getImagePath())
                ))
                .forEach(rows::add);

        return rows.stream()
                .filter(row -> !suspiciousOnly || row.suspicious())
                .sorted(Comparator.comparing(AdminImageRowDto::suspicious).reversed().thenComparing(AdminImageRowDto::ownerName))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminSkillImageGroupDto> getSkillImageGroups() {
        return announceImageService.getSkillImageCatalog().entrySet().stream()
                .map(entry -> new AdminSkillImageGroupDto(formatSkillLabel(entry.getKey()), entry.getValue()))
                .toList();
    }

    @Override
    @Transactional
    public void deleteImage(String imageType, Long entityId, Long adminUserId) {
        String normalizedType = normalizeQuery(imageType).toUpperCase(Locale.ROOT);
        if ("PROFILE".equals(normalizedType)) {
            Profil profile = profileRepository.findById(entityId)
                    .orElseThrow(() -> new IllegalArgumentException("Profile image not found."));
            profile.setImageUrl(null);
            if (profile.getUser() != null) {
                destroyCloudinaryImage(PROFILE_IMAGE_PUBLIC_ID_PREFIX + profile.getUser().getId());
            }
            logAction(adminUserId, AdminActionType.IMAGE_DELETED, "PROFILE", entityId, profile.getName(), "Profile image removed.");
            return;
        }

        if ("POST".equals(normalizedType)) {
            Announce announce = announceRepository.findById(entityId)
                    .orElseThrow(() -> new IllegalArgumentException("Post image not found."));
            announce.setImageKey(null);
            announce.setImagePath(null);
            logAction(adminUserId, AdminActionType.IMAGE_DELETED, "POST", entityId, announce.getTitle(), "Post image reference removed.");
            return;
        }

        throw new IllegalArgumentException("Unsupported image type.");
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminSettingRowDto> getSettings() {
        return platformSettingRepository.findAllByOrderByCategoryAscLabelAsc().stream()
                .map(setting -> new AdminSettingRowDto(
                        setting.getSettingKey(),
                        setting.getLabel(),
                        setting.getCategory(),
                        setting.getSettingValue(),
                        setting.getDescription()
                ))
                .toList();
    }

    @Override
    @Transactional
    public void updateSettings(Map<String, String> valuesByKey, Long adminUserId) {
        User admin = userRepository.findById(adminUserId)
                .orElseThrow(() -> new IllegalArgumentException("Admin not found."));

        platformSettingRepository.findAll().forEach(setting -> {
            if (valuesByKey.containsKey(setting.getSettingKey())) {
                setting.setSettingValue(valuesByKey.get(setting.getSettingKey()));
                setting.setUpdatedBy(admin);
            }
        });

        logAction(adminUserId, AdminActionType.SETTINGS_UPDATED, "SETTINGS", null, "Platform settings", "Updated platform configuration values.");
    }

    private List<User> activeUsers() {
        return userRepository.findAll().stream()
                .filter(user -> !user.isDeleted())
                .toList();
    }

    private AdminSummaryMetricDto metric(String label, long value, String context, String tone) {
        return new AdminSummaryMetricDto(label, Long.toString(value), context, tone);
    }

    private List<AdminChartPointDto> buildUserRegistrationSeries(List<User> users, int days) {
        LocalDate today = LocalDate.now();
        Map<LocalDate, Long> values = new LinkedHashMap<>();
        for (int offset = days - 1; offset >= 0; offset--) {
            values.put(today.minusDays(offset), 0L);
        }
        users.stream()
                .map(User::getRegisterData)
                .filter(Objects::nonNull)
                .map(LocalDateTime::toLocalDate)
                .filter(values::containsKey)
                .forEach(date -> values.computeIfPresent(date, (ignored, count) -> count + 1));

        return values.entrySet().stream()
                .map(entry -> new AdminChartPointDto(entry.getKey().getDayOfMonth() + " " + entry.getKey().getMonth().name().substring(0, 3), entry.getValue()))
                .toList();
    }

    private List<AdminChartPointDto> buildSessionsByWeekSeries(List<SkillSwapProposal> proposals, int weeks) {
        LocalDate currentWeek = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        Map<LocalDate, Long> values = new LinkedHashMap<>();
        for (int offset = weeks - 1; offset >= 0; offset--) {
            values.put(currentWeek.minusWeeks(offset), 0L);
        }
        proposals.stream()
                .map(SkillSwapProposal::getCreatedAt)
                .filter(Objects::nonNull)
                .map(date -> date.toLocalDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)))
                .filter(values::containsKey)
                .forEach(date -> values.computeIfPresent(date, (ignored, count) -> count + 1));

        return values.entrySet().stream()
                .map(entry -> new AdminChartPointDto("Week of " + entry.getKey().getDayOfMonth() + "/" + entry.getKey().getMonthValue(), entry.getValue()))
                .toList();
    }

    private List<AdminChartPointDto> buildProposalStatusSeries(List<SkillSwapProposal> proposals, Collection<SkillSwapProposalStatus> statuses) {
        Map<LocalDate, Long> values = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();
        for (int offset = 13; offset >= 0; offset--) {
            values.put(today.minusDays(offset), 0L);
        }
        proposals.stream()
                .filter(proposal -> statuses.contains(proposal.getStatus()))
                .map(SkillSwapProposal::getCreatedAt)
                .filter(Objects::nonNull)
                .map(LocalDateTime::toLocalDate)
                .filter(values::containsKey)
                .forEach(date -> values.computeIfPresent(date, (ignored, count) -> count + 1));

        return values.entrySet().stream()
                .map(entry -> new AdminChartPointDto(entry.getKey().getDayOfMonth() + "/" + entry.getKey().getMonthValue(), entry.getValue()))
                .toList();
    }

    private List<AdminChartPointDto> buildPopularSkillsSeries(int limit) {
        return buildSkillInventory().values().stream()
                .sorted(Comparator.comparing(AdminSkillAggregate::occurrences).reversed())
                .limit(limit)
                .map(skill -> new AdminChartPointDto(skill.canonicalName(), skill.occurrences()))
                .toList();
    }

    private Map<String, AdminSkillAggregate> buildSkillInventory() {
        Map<String, AdminSkillAggregate> aggregateMap = new LinkedHashMap<>();
        Stream.concat(
            announceRepository.findAll().stream()
                .filter(announce -> !announce.isDeletedByAdmin())
                .flatMap(announce -> Stream.of(announce.getCategoryOffered(), announce.getCategoryRequired())),
                skillSwapProposalRepository.findAll().stream().flatMap(proposal -> Stream.of(proposal.getOfferedSkill(), proposal.getRequestedSkill()))
        )
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(skill -> !skill.isBlank())
                .forEach(skill -> {
                    String normalized = normalizeSkill(skill);
                    aggregateMap.compute(normalized, (ignored, current) -> {
                        if (current == null) {
                            LinkedHashSet<String> variants = new LinkedHashSet<>();
                            variants.add(skill);
                            return new AdminSkillAggregate(skill, 1, variants);
                        }
                        current.variants().add(skill);
                        String canonical = current.canonicalName().length() >= skill.length() ? current.canonicalName() : skill;
                        return new AdminSkillAggregate(canonical, current.occurrences() + 1, current.variants());
                    });
                });
        return aggregateMap;
    }

    private boolean matchesUser(User user, String needle) {
        if (needle.isBlank()) {
            return true;
        }

        boolean matchesBasicFields = containsIgnoreCase(user.getEmail(), needle)
                || containsIgnoreCase(user.getFullName(), needle);
        boolean matchesSkills = announceRepository.getAnnouncesListByEmail(user.getId()).stream()
                .anyMatch(announce -> containsIgnoreCase(announce.getCategoryOffered(), needle)
                        || containsIgnoreCase(announce.getCategoryRequired(), needle));

        return matchesBasicFields || matchesSkills;
    }

    private boolean matchesUserStatus(User user, String status) {
        String normalized = normalizeQuery(status);
        if (normalized.isBlank()) {
            return true;
        }
        return switch (normalized) {
            case "active" -> !user.isSuspended() && !user.isBanned() && !user.isDeleted();
            case "suspended" -> user.isSuspended();
            case "banned" -> user.isBanned();
            case "deleted" -> user.isDeleted();
            default -> true;
        };
    }

    private boolean matchesPost(Announce announce, String needle) {
        return needle.isBlank()
                || containsIgnoreCase(announce.getTitle(), needle)
                || containsIgnoreCase(announce.getAuthor(), needle)
                || containsIgnoreCase(announce.getCategoryOffered(), needle)
                || containsIgnoreCase(announce.getCategoryRequired(), needle)
                || containsIgnoreCase(announce.getDescription(), needle);
    }

    private boolean matchesReview(ProfileComment comment, String needle) {
        return needle.isBlank()
                || containsIgnoreCase(comment.getAuthorDisplayName(), needle)
                || containsIgnoreCase(comment.getProfileOwner().getFullName(), needle)
                || containsIgnoreCase(comment.getContent(), needle);
    }

    private AdminReportRowDto toReportRow(ModerationReport report) {
        return new AdminReportRowDto(
                report.getId(),
                report.getReporter() != null ? report.getReporter().getEmail() : "System",
                report.getTargetLabel(),
                report.getReason(),
                report.getTargetType().name(),
                report.getStatus().name(),
                report.getCreatedAt()
        );
    }

    private AdminRecentActionDto toRecentAction(AdminAuditLog log) {
        String label = log.getTargetLabel() == null || log.getTargetLabel().isBlank() ? log.getTargetType() : log.getTargetLabel();
        return new AdminRecentActionDto(
                log.getAdminUser().getEmail(),
                log.getActionType().name().replace('_', ' ') + " - " + label,
                log.getCreatedAt()
        );
    }

    private void warnTargetUser(ModerationReport report) {
        User targetUser = resolveTargetUser(report);
        notificationService.createNotification(
                targetUser.getId(),
                NotificationType.SYSTEM,
                "Account warning",
                "A report was validated by the moderation team. Please review the platform rules.",
                "/profile"
        );
    }

    private void deleteReportedContent(ModerationReport report, Long adminUserId) {
        if (report.getTargetType() == ReportTargetType.POST) {
            deletePost(report.getTargetId(), adminUserId);
            return;
        }
        if (report.getTargetType() == ReportTargetType.REVIEW) {
            deleteReview(report.getTargetId(), adminUserId);
            return;
        }
        if (report.getTargetType() == ReportTargetType.IMAGE) {
            deleteImage("PROFILE", report.getTargetId(), adminUserId);
            return;
        }

        throw new IllegalArgumentException("This report target cannot be deleted as content.");
    }

    private User resolveTargetUser(ModerationReport report) {
        if (report.getTargetType() == ReportTargetType.USER) {
            return getManagedUser(report.getTargetId());
        }
        if (report.getTargetType() == ReportTargetType.POST) {
            Announce announce = announceRepository.findById(report.getTargetId())
                    .orElseThrow(() -> new IllegalArgumentException("Reported post not found."));
            return announce.getUser();
        }
        if (report.getTargetType() == ReportTargetType.REVIEW) {
            ProfileComment review = profileCommentRepository.findById(report.getTargetId())
                    .orElseThrow(() -> new IllegalArgumentException("Reported review not found."));
            return review.getAuthor();
        }
        if (report.getTargetType() == ReportTargetType.IMAGE) {
            Profil profile = profileRepository.findById(report.getTargetId())
                    .orElseThrow(() -> new IllegalArgumentException("Reported image not found."));
            return profile.getUser();
        }
        throw new IllegalArgumentException("Unsupported report target.");
    }

    private User getManagedUser(Long targetUserId) {
        return userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
    }

    private void logAction(Long adminUserId, AdminActionType actionType, String targetType, Long targetId, String targetLabel, String details) {
        User admin = userRepository.findById(adminUserId)
                .orElseThrow(() -> new IllegalArgumentException("Admin not found."));
        AdminAuditLog log = new AdminAuditLog();
        log.setAdminUser(admin);
        log.setActionType(actionType);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setTargetLabel(targetLabel);
        log.setDetails(details);
        adminAuditLogRepository.save(log);
    }

    private void destroyCloudinaryImage(String publicId) {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.asMap(
                    "resource_type", "image",
                    "invalidate", true
            ));
        } catch (Exception ignored) {
            // Keep DB moderation responsive even if cloud deletion fails.
        }
    }

    private boolean isSuspiciousImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return false;
        }
        return !imageUrl.contains("res.cloudinary.com") && !imageUrl.startsWith("/img/");
    }

    private String rewriteSkill(String currentValue, String targetName, Set<String> normalizedSources) {
        return normalizedSources.contains(normalizeSkill(currentValue)) ? targetName : currentValue;
    }

    private String blankFallback(String value) {
        return value == null || value.isBlank() ? "Not set" : value;
    }

    private String normalizeQuery(String query) {
        return query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeSkill(String skill) {
        if (skill == null) {
            return "";
        }
        return skill.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
    }

    private String formatSkillLabel(String rawLabel) {
        if (rawLabel == null || rawLabel.isBlank()) {
            return "Unknown skill";
        }

        return Arrays.stream(rawLabel.split("\\s+"))
                .filter(part -> !part.isBlank())
                .map(part -> Character.toUpperCase(part.charAt(0)) + part.substring(1))
                .collect(Collectors.joining(" "));
    }

    private boolean containsIgnoreCase(String source, String needle) {
        return source != null && source.toLowerCase(Locale.ROOT).contains(needle);
    }

    private boolean isSameDay(LocalDateTime value, LocalDate expectedDate) {
        return value != null && value.toLocalDate().equals(expectedDate);
    }

    private <T> Page<T> paginate(List<T> items, int page, int size) {
        int safeSize = size <= 0 ? DEFAULT_PAGE_SIZE : size;
        int safePage = Math.max(page, 0);
        int start = Math.min(safePage * safeSize, items.size());
        int end = Math.min(start + safeSize, items.size());
        return new PageImpl<>(items.subList(start, end), PageRequest.of(safePage, safeSize), items.size());
    }

    private record AdminSkillAggregate(String canonicalName, long occurrences, LinkedHashSet<String> variants) {
    }
}