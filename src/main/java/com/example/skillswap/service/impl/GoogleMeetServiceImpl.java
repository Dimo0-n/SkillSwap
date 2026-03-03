package com.example.skillswap.service.impl;

import com.example.skillswap.dto.GoogleMeetSpaceDto;
import com.example.skillswap.exceptions.GoogleApiException;
import com.example.skillswap.service.GoogleMeetService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleMeetServiceImpl implements GoogleMeetService {

    private final RestClient.Builder restClientBuilder;

    @Value("${app.google.meet.base-url:https://meet.googleapis.com}")
    private String googleMeetBaseUrl;

    @Override
    public GoogleMeetSpaceDto createSpace(OAuth2AuthorizedClient client) {
        RestClient restClient = restClientBuilder.baseUrl(googleMeetBaseUrl).build();

        try {
            JsonNode response = restClient.post()
                    .uri("/v2/spaces")
                    .headers(headers -> headers.setBearerAuth(client.getAccessToken().getTokenValue()))
                    .retrieve()
                    .body(JsonNode.class);

            if (response == null) {
                throw new GoogleApiException("Google Meet returned empty response");
            }

            String spaceName = response.path("name").asText(null);
            String meetingUrl = response.path("meetingUri").asText(null);
            String meetingCode = response.path("meetingCode").asText(null);

            if (spaceName == null || meetingUrl == null || meetingCode == null) {
                log.error("Invalid Google Meet create response payload: {}", response);
                throw new GoogleApiException("Google Meet response missing required fields");
            }

            return new GoogleMeetSpaceDto(spaceName, meetingUrl, meetingCode);
        } catch (RestClientResponseException e) {
            HttpStatus status = HttpStatus.resolve(e.getStatusCode().value());
            log.error("Google Meet create failed. status={}, body={}", status, e.getResponseBodyAsString(), e);
            throw new GoogleApiException("Failed to create Google Meet space");
        } catch (Exception e) {
            log.error("Unexpected error while creating Google Meet space", e);
            throw new GoogleApiException("Failed to create Google Meet space");
        }
    }

    @Override
    public boolean validateSpace(String spaceName, OAuth2AuthorizedClient client) {
        RestClient restClient = restClientBuilder.baseUrl(googleMeetBaseUrl).build();

        try {
            restClient.get()
                    .uri("/v2/{spaceName}", spaceName)
                    .headers(headers -> headers.setBearerAuth(client.getAccessToken().getTokenValue()))
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (RestClientResponseException e) {
            int status = e.getStatusCode().value();
            if (status == 404 || status == 410) {
                return false;
            }

            log.error("Google Meet validate failed. space={}, status={}, body={}",
                    spaceName,
                    status,
                    e.getResponseBodyAsString(),
                    e);
            throw new GoogleApiException("Failed to validate Google Meet space");
        } catch (Exception e) {
            log.error("Unexpected error while validating Google Meet space={}", spaceName, e);
            throw new GoogleApiException("Failed to validate Google Meet space");
        }
    }
}
