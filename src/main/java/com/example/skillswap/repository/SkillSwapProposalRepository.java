package com.example.skillswap.repository;

import com.example.skillswap.entity.SkillSwapProposal;
import com.example.skillswap.enums.SkillSwapProposalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SkillSwapProposalRepository extends JpaRepository<SkillSwapProposal, Long> {

    boolean existsByAnnounceIdAndRequesterIdAndStatus(Long announceId, Long requesterId, SkillSwapProposalStatus status);

    Optional<SkillSwapProposal> findByIdAndOwnerId(Long id, Long ownerId);
}
