package com.example.skillswap.service;

import com.example.skillswap.dto.GoogleMeetSpaceDto;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;

public interface GoogleMeetService {

    GoogleMeetSpaceDto createSpace(OAuth2AuthorizedClient client);

    boolean validateSpace(String spaceName, OAuth2AuthorizedClient client);
}
