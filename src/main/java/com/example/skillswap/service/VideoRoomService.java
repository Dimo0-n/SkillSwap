package com.example.skillswap.service;

import com.example.skillswap.dto.GoogleMeetSpaceDto;
import com.example.skillswap.dto.VideoRoomResponseDto;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;

public interface VideoRoomService {

    VideoRoomResponseDto getOrCreateVideoRoom(Long conversationId, Authentication authentication);

    GoogleMeetSpaceDto createSpace(OAuth2AuthorizedClient client);

    boolean validateSpace(String spaceName, OAuth2AuthorizedClient client);
}
