package com.example.skillswap.controller;

import com.example.skillswap.admin.dto.AdminDashboardOverviewDto;
import com.example.skillswap.admin.dto.AdminImageRowDto;
import com.example.skillswap.admin.dto.AdminStatisticsDto;
import com.example.skillswap.admin.service.AdminCacheStatsService;
import com.example.skillswap.admin.service.AdminDashboardService;
import com.example.skillswap.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminController {

    private final AdminDashboardService adminDashboardService;
    private final AdminCacheStatsService adminCacheStatsService;
    private final ChatService chatService;

    @GetMapping
    public String dashboard(Model model) {
        AdminDashboardOverviewDto overview = adminDashboardService.getOverview();
        populateBaseModel(model, "dashboard");
        model.addAttribute("overview", overview);
        return "admin/panel";
    }

    @GetMapping("/users")
    public String users(@RequestParam(required = false) String query,
                        @RequestParam(required = false) String status,
                        @RequestParam(defaultValue = "0") int page,
                        Model model) {
        populateBaseModel(model, "users");
        model.addAttribute("query", query == null ? "" : query);
        model.addAttribute("status", status == null ? "" : status);
        model.addAttribute("usersPage", adminDashboardService.getUsers(query, status, page, 10));
        return "admin/panel";
    }

    @GetMapping("/skills")
    public String skills(@RequestParam(required = false) String query, Model model) {
        populateBaseModel(model, "skills");
        model.addAttribute("query", query == null ? "" : query);
        model.addAttribute("skills", adminDashboardService.getSkills(query));
        return "admin/panel";
    }

    @GetMapping("/posts")
    public String posts(@RequestParam(required = false) String query,
                        @RequestParam(defaultValue = "false") boolean spamOnly,
                        @RequestParam(defaultValue = "0") int page,
                        Model model) {
        populateBaseModel(model, "posts");
        model.addAttribute("query", query == null ? "" : query);
        model.addAttribute("spamOnly", spamOnly);
        model.addAttribute("postsPage", adminDashboardService.getPosts(query, spamOnly, page, 10));
        return "admin/panel";
    }

    @GetMapping("/reviews")
    public String reviews(@RequestParam(required = false) String query,
                          @RequestParam(required = false) Integer rating,
                          @RequestParam(defaultValue = "false") boolean reportedOnly,
                          @RequestParam(defaultValue = "0") int page,
                          Model model) {
        populateBaseModel(model, "reviews");
        model.addAttribute("query", query == null ? "" : query);
        model.addAttribute("rating", rating);
        model.addAttribute("reportedOnly", reportedOnly);
        model.addAttribute("reviewsPage", adminDashboardService.getReviews(query, rating, reportedOnly, page, 10));
        return "admin/panel";
    }

    @GetMapping("/reports")
    public String reports(@RequestParam(required = false) String status,
                          @RequestParam(defaultValue = "0") int page,
                          Model model) {
        populateBaseModel(model, "reports");
        model.addAttribute("status", status == null ? "" : status);
        model.addAttribute("reportsPage", adminDashboardService.getReports(status, page, 10));
        return "admin/panel";
    }

    @GetMapping("/statistics")
    public String statistics(Model model) {
        AdminStatisticsDto statistics = adminDashboardService.getStatistics();
        populateBaseModel(model, "statistics");
        model.addAttribute("statistics", statistics);
        return "admin/panel";
    }

    @GetMapping("/cache")
    public String cache(Model model) {
        populateBaseModel(model, "cache");
        model.addAttribute("cacheStats", adminCacheStatsService.getAllCacheStats());
        return "admin/panel";
    }

    @GetMapping("/notifications")
    public String notifications(Model model) {
        populateBaseModel(model, "notifications");
        model.addAttribute("recentNotifications", adminDashboardService.getRecentNotifications());
        return "admin/panel";
    }

    @GetMapping("/images")
    public String images(@RequestParam(defaultValue = "skills") String module,
                         @RequestParam(defaultValue = "false") boolean suspiciousOnly,
                         Model model) {
        populateBaseModel(model, "images");
        String normalizedModule = "profiles".equalsIgnoreCase(module) ? "profiles" : "skills";
        List<AdminImageRowDto> profileImages = adminDashboardService.getImages(suspiciousOnly).stream()
            .filter(image -> "PROFILE".equals(image.imageType()))
            .toList();
        model.addAttribute("imageModule", normalizedModule);
        model.addAttribute("suspiciousOnly", suspiciousOnly);
        model.addAttribute("profileImages", profileImages);
        model.addAttribute("skillImageGroups", adminDashboardService.getSkillImageGroups());
        return "admin/panel";
    }

    @GetMapping("/settings")
    public String settings(Model model) {
        populateBaseModel(model, "settings");
        model.addAttribute("settingsRows", adminDashboardService.getSettings());
        return "admin/panel";
    }

    @PostMapping("/users/{userId}/suspend")
    public String suspendUser(@PathVariable Long userId,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        adminDashboardService.suspendUser(userId, adminId(authentication));
        redirectAttributes.addFlashAttribute("adminSuccess", "User suspended.");
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{userId}/ban")
    public String banUser(@PathVariable Long userId,
                          Authentication authentication,
                          RedirectAttributes redirectAttributes) {
        adminDashboardService.banUser(userId, adminId(authentication));
        redirectAttributes.addFlashAttribute("adminSuccess", "User banned.");
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{userId}/grant-admin")
    public String grantAdminRole(@PathVariable Long userId,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        adminDashboardService.grantAdminRole(userId, adminId(authentication));
        redirectAttributes.addFlashAttribute("adminSuccess", "Admin role assigned.");
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{userId}/delete")
    public String deleteUser(@PathVariable Long userId,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        adminDashboardService.softDeleteUser(userId, adminId(authentication));
        redirectAttributes.addFlashAttribute("adminSuccess", "User soft-deleted.");
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{userId}/reset-password")
    public String resetPassword(@PathVariable Long userId,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        String temporaryPassword = adminDashboardService.resetPassword(userId, adminId(authentication));
        redirectAttributes.addFlashAttribute("adminSuccess", "Temporary password: " + temporaryPassword);
        return "redirect:/admin/users";
    }

    @PostMapping("/skills/merge")
    public String mergeSkills(@RequestParam String targetName,
                              @RequestParam(name = "sourceNames", required = false) List<String> sourceNames,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        adminDashboardService.mergeSkills(targetName, sourceNames, adminId(authentication));
        redirectAttributes.addFlashAttribute("adminSuccess", "Skill aliases merged.");
        return "redirect:/admin/skills";
    }

    @PostMapping("/skills/delete")
    public String deleteSkill(@RequestParam String skillName,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        adminDashboardService.deleteSkill(skillName, adminId(authentication));
        redirectAttributes.addFlashAttribute("adminSuccess", "Skill removed from stored content.");
        return "redirect:/admin/skills";
    }

    @PostMapping("/posts/{postId}/delete")
    public String deletePost(@PathVariable Long postId,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        adminDashboardService.deletePost(postId, adminId(authentication));
        redirectAttributes.addFlashAttribute("adminSuccess", "Post deleted.");
        return "redirect:/admin/posts";
    }

    @PostMapping("/posts/{postId}/spam")
    public String markPostSpam(@PathVariable Long postId,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        adminDashboardService.markPostAsSpam(postId, adminId(authentication));
        redirectAttributes.addFlashAttribute("adminSuccess", "Post marked as spam.");
        return "redirect:/admin/posts";
    }

    @PostMapping("/reviews/{reviewId}/delete")
    public String deleteReview(@PathVariable Long reviewId,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        adminDashboardService.deleteReview(reviewId, adminId(authentication));
        redirectAttributes.addFlashAttribute("adminSuccess", "Review deleted.");
        return "redirect:/admin/reviews";
    }

    @PostMapping("/reports/{reportId}/resolve")
    public String resolveReport(@PathVariable Long reportId,
                                @RequestParam String action,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        adminDashboardService.resolveReport(reportId, action, adminId(authentication));
        redirectAttributes.addFlashAttribute("adminSuccess", "Report action applied.");
        return "redirect:/admin/reports";
    }

    @PostMapping("/notifications/send")
    public String sendNotification(@RequestParam String title,
                                   @RequestParam String message,
                                   @RequestParam(required = false) String targetUrl,
                                   Authentication authentication,
                                   RedirectAttributes redirectAttributes) {
        int recipients = adminDashboardService.sendPlatformAnnouncement(title, message, targetUrl, adminId(authentication));
        redirectAttributes.addFlashAttribute("adminSuccess", "Announcement sent to " + recipients + " users.");
        return "redirect:/admin/notifications";
    }

    @PostMapping("/images/delete")
    public String deleteImage(@RequestParam String imageType,
                              @RequestParam Long entityId,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        adminDashboardService.deleteImage(imageType, entityId, adminId(authentication));
        redirectAttributes.addFlashAttribute("adminSuccess", "Image removed.");
        return "redirect:/admin/images";
    }

    @PostMapping("/settings")
    public String updateSettings(@RequestParam Map<String, String> values,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        Map<String, String> normalizedValues = values.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith("setting_"))
                .collect(Collectors.toMap(
                        entry -> entry.getKey().substring("setting_".length()),
                        Map.Entry::getValue
                ));
        adminDashboardService.updateSettings(normalizedValues, adminId(authentication));
        redirectAttributes.addFlashAttribute("adminSuccess", "Platform settings saved.");
        return "redirect:/admin/settings";
    }

    private void populateBaseModel(Model model, String activeSection) {
        model.addAttribute("activeSection", activeSection);
    }

    private Long adminId(Authentication authentication) {
        return chatService.getCurrentUserId(authentication);
    }
}
