package com.example.skillswap.config;

import com.example.skillswap.dto.PresenceStatusDTO;
import com.example.skillswap.service.ChatService;
import com.example.skillswap.service.UserTimeZoneService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class AuthenticatedUserPresenceFilter extends OncePerRequestFilter {

    private static final String TIME_ZONE_HEADER = "X-Time-Zone";

    private final ChatService chatService;
    private final UserTimeZoneService userTimeZoneService;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            try {
                Long userId = chatService.getCurrentUserId(authentication);
                syncTimeZoneFromHeader(request, userId);
                boolean becameOnline = chatService.setUserOnline(userId);
                if (becameOnline) {
                    var user = chatService.getUserById(userId);
                    messagingTemplate.convertAndSend(
                            "/topic/presence",
                            new PresenceStatusDTO(userId, true, null, user.getTimeZoneId())
                    );
                }
            } catch (Exception ignored) {
                // Ignore non-standard authenticated principals that cannot be resolved to a local user.
            }
        }

        filterChain.doFilter(request, response);
    }

    private void syncTimeZoneFromHeader(HttpServletRequest request, Long userId) {
        String timeZoneId = request.getHeader(TIME_ZONE_HEADER);
        if (timeZoneId == null || timeZoneId.isBlank()) {
            return;
        }

        try {
            userTimeZoneService.updateTimeZone(userId, timeZoneId);
        } catch (IllegalArgumentException ignored) {
            // Ignore invalid client-provided time zone values.
        }
    }
}
