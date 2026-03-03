package com.example.skillswap.service.impl;

import com.example.skillswap.dto.GoogleMeetSpaceDto;
import com.example.skillswap.dto.VideoRoomResponseDto;
import com.example.skillswap.dto.VideoSessionReadyDto;
import com.example.skillswap.entity.ChatRoom;
import com.example.skillswap.entity.VideoRoom;
import com.example.skillswap.exceptions.ApiException;
import com.example.skillswap.repository.ChatRoomRepository;
import com.example.skillswap.repository.UserRepository;
import com.example.skillswap.repository.VideoRoomRepository;
import com.example.skillswap.security.CustomUserDetails;
import com.example.skillswap.service.GoogleMeetService;
import com.example.skillswap.service.VideoRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoRoomServiceImpl implements VideoRoomService {

    private static final String GOOGLE_REGISTRATION_ID = "google";

    private final ChatRoomRepository chatRoomRepository;
    private final VideoRoomRepository videoRoomRepository;
    private final UserRepository userRepository;
    private final OAuth2AuthorizedClientService authorizedClientService;
    private final OAuth2AuthorizedClientRepository authorizedClientRepository;
    private final GoogleMeetService googleMeetService;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    public VideoRoomResponseDto getOrCreateVideoRoom(Long conversationId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }

        Long currentUserId = extractCurrentUserId(authentication);

        ChatRoom chatRoom = chatRoomRepository.findByIdForUpdate(conversationId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Conversation not found"));

        ensureUserBelongsToConversation(chatRoom, currentUserId);

        OAuth2AuthorizedClient client = resolveAuthorizedClient(authentication);
        ensureTokenUsable(client);

        VideoRoom existingActive = videoRoomRepository
                .findFirstByChatRoomIdAndActiveTrueOrderByCreatedAtDesc(chatRoom.getId())
                .orElse(null);

        if (existingActive == null) {
            VideoRoom created = createAndPersistRoom(chatRoom, client);
            publishVideoSessionReady(chatRoom.getId(), created.getMeetingUrl(), currentUserId);
            return new VideoRoomResponseDto(created.getMeetingUrl(), created.getMeetingCode(), created.getCreatedAt(), false);
        }

        Instant now = Instant.now();
        Instant validateThreshold = now.minusSeconds(24 * 60 * 60);
        Instant lastValidatedAt = existingActive.getLastValidatedAt();

        if (lastValidatedAt != null && lastValidatedAt.isAfter(validateThreshold)) {
            publishVideoSessionReady(chatRoom.getId(), existingActive.getMeetingUrl(), currentUserId);
            return new VideoRoomResponseDto(existingActive.getMeetingUrl(), existingActive.getMeetingCode(), existingActive.getCreatedAt(), true);
        }

        boolean valid = validateSpace(existingActive.getSpaceName(), client);
        existingActive.setLastValidatedAt(now);

        if (valid) {
            videoRoomRepository.save(existingActive);
            publishVideoSessionReady(chatRoom.getId(), existingActive.getMeetingUrl(), currentUserId);
            return new VideoRoomResponseDto(existingActive.getMeetingUrl(), existingActive.getMeetingCode(), existingActive.getCreatedAt(), true);
        }

        existingActive.setActive(false);
        videoRoomRepository.save(existingActive);

        VideoRoom created = createAndPersistRoom(chatRoom, client);
        publishVideoSessionReady(chatRoom.getId(), created.getMeetingUrl(), currentUserId);
        return new VideoRoomResponseDto(created.getMeetingUrl(), created.getMeetingCode(), created.getCreatedAt(), false);
    }

    @Override
    public GoogleMeetSpaceDto createSpace(OAuth2AuthorizedClient client) {
        return googleMeetService.createSpace(client);
    }

    @Override
    public boolean validateSpace(String spaceName, OAuth2AuthorizedClient client) {
        return googleMeetService.validateSpace(spaceName, client);
    }

    private void ensureUserBelongsToConversation(ChatRoom chatRoom, Long currentUserId) {
        boolean isMember = chatRoom.getUser1().getId().equals(currentUserId)
                || chatRoom.getUser2().getId().equals(currentUserId);
        if (!isMember) {
            throw new ApiException(HttpStatus.FORBIDDEN, "User is not part of conversation");
        }
    }

    private OAuth2AuthorizedClient resolveAuthorizedClient(Authentication authentication) {
        Set<String> principalCandidates = new LinkedHashSet<>();
        principalCandidates.add(authentication.getName());

        Object principal = authentication.getPrincipal();
        if (principal instanceof OAuth2User oauth2User) {
            principalCandidates.add(oauth2User.getName());

            String email = oauth2User.getAttribute("email");
            if (email != null && !email.isBlank()) {
                principalCandidates.add(email);
            }

            String sub = oauth2User.getAttribute("sub");
            if (sub != null && !sub.isBlank()) {
                principalCandidates.add(sub);
            }
        }

        OAuth2AuthorizedClient client = null;
        for (String principalName : principalCandidates) {
            if (principalName == null || principalName.isBlank()) {
                continue;
            }
            client = authorizedClientService.loadAuthorizedClient(GOOGLE_REGISTRATION_ID, principalName);
            if (client != null) {
                break;
            }
        }

        if (client == null) {
            RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
            if (requestAttributes instanceof ServletRequestAttributes servletRequestAttributes) {
                HttpServletRequest request = servletRequestAttributes.getRequest();
                client = authorizedClientRepository.loadAuthorizedClient(GOOGLE_REGISTRATION_ID, authentication, request);
            }
        }

        if (client == null) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Google account is not connected");
        }

        return client;
    }

    private void ensureTokenUsable(OAuth2AuthorizedClient client) {
        Instant expiresAt = client.getAccessToken().getExpiresAt();
        Instant now = Instant.now();

        if (expiresAt == null || expiresAt.isBefore(now.plusSeconds(30))) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Google access token expired. Please re-login.");
        }
    }

    private VideoRoom createAndPersistRoom(ChatRoom chatRoom, OAuth2AuthorizedClient client) {
        videoRoomRepository.findByChatRoomIdAndActiveTrue(chatRoom.getId())
                .forEach(room -> {
                    room.setActive(false);
                    videoRoomRepository.save(room);
                });

        GoogleMeetSpaceDto createdSpace = createSpace(client);

        VideoRoom videoRoom = new VideoRoom();
        videoRoom.setChatRoom(chatRoom);
        videoRoom.setSpaceName(createdSpace.getSpaceName());
        videoRoom.setMeetingUrl(createdSpace.getMeetingUrl());
        videoRoom.setMeetingCode(createdSpace.getMeetingCode());
        videoRoom.setActive(true);
        videoRoom.setCreatedAt(Instant.now());
        videoRoom.setLastValidatedAt(Instant.now());

        VideoRoom saved = videoRoomRepository.save(videoRoom);

        return saved;
    }

    private void publishVideoSessionReady(Long chatRoomId, String meetingUrl, Long createdByUserId) {
        try {
            String destination = "/topic/chat/" + chatRoomId + "/video";
            log.info("[VIDEO] Publishing to {} - meetingUrl={} createdByUserId={}", destination, meetingUrl, createdByUserId);
            messagingTemplate.convertAndSend(
                    destination,
                    new VideoSessionReadyDto("Video session ready", meetingUrl, createdByUserId));
            log.info("[VIDEO] Published successfully to {}", destination);
        } catch (Exception e) {
            log.warn("Failed to publish video room websocket event for conversation={}", chatRoomId, e);
        }
    }

    private Long extractCurrentUserId(Authentication authentication) {
        Object principal = authentication.getPrincipal();

        if (principal instanceof CustomUserDetails customUserDetails) {
            return customUserDetails.getId();
        }

        if (principal instanceof OAuth2User oauth2User) {
            Object userId = oauth2User.getAttribute("userId");
            if (userId instanceof Number number) {
                return number.longValue();
            }

            String email = oauth2User.getAttribute("email");
            if (email != null && !email.isBlank()) {
                return userRepository.findByEmail(email)
                        .map(user -> user.getId())
                        .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Not authenticated"));
            }
        }

        throw new ApiException(HttpStatus.UNAUTHORIZED, "Not authenticated");
    }
}
