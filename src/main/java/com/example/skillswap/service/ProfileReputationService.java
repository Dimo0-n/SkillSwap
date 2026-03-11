package com.example.skillswap.service;

public interface ProfileReputationService {

    void refreshProfileReputationIfNeeded(Long profileOwnerId);
}
