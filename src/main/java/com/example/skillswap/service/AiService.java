package com.example.skillswap.service;

import com.example.skillswap.dto.ProfileReputationResult;

import java.util.List;

public interface AiService {

    String generateDescription(String ideas);

    ProfileReputationResult evaluateProfileReputation(List<String> feedbackComments);
}
