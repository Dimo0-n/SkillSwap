package com.example.skillswap.event;

import com.example.skillswap.service.ProfileReputationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ProfileReputationRefreshRequestedListener {

    private final ProfileReputationService profileReputationService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handle(ProfileReputationRefreshRequestedEvent event) {
        profileReputationService.refreshProfileReputationIfNeeded(event.profileOwnerId());
    }
}
