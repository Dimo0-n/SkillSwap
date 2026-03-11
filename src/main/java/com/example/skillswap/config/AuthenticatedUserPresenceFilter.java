package com.example.skillswap.config;

import com.example.skillswap.dto.PresenceStatusDTO;
import com.example.skillswap.service.ChatService;
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

    private final ChatService chatService;
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
                boolean becameOnline = chatService.setUserOnline(userId);
                if (becameOnline) {
                    messagingTemplate.convertAndSend("/topic/presence", new PresenceStatusDTO(userId, true, null));
                }
            } catch (Exception ignored) {
                // Ignore non-standard authenticated principals that cannot be resolved to a local user.
            }
        }

        filterChain.doFilter(request, response);
    }
}
