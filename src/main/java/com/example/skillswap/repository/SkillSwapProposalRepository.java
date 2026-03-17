package com.example.skillswap.repository;

import com.example.skillswap.entity.SkillSwapProposal;
import com.example.skillswap.enums.SkillSwapProposalStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;

@Repository
public interface SkillSwapProposalRepository extends JpaRepository<SkillSwapProposal, Long> {

    boolean existsByAnnounceIdAndRequesterIdAndStatus(Long announceId, Long requesterId, SkillSwapProposalStatus status);

    boolean existsByAnnounceIdAndRequesterIdAndStatusIn(Long announceId,
                                                        Long requesterId,
                                                        Collection<SkillSwapProposalStatus> statuses);

    Optional<SkillSwapProposal> findTopByAnnounceIdAndRequesterIdAndStatusInOrderByCreatedAtDesc(Long announceId,
                                                                                                  Long requesterId,
                                                                                                  Collection<SkillSwapProposalStatus> statuses);

    Optional<SkillSwapProposal> findTopByChatRoomIdAndStatusInOrderByUpdatedAtDesc(Long chatRoomId,
                                                                                   Collection<SkillSwapProposalStatus> statuses);

    Optional<SkillSwapProposal> findByIdAndOwnerId(Long id, Long ownerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select proposal
        from SkillSwapProposal proposal
        where proposal.id = :id
    """)
    Optional<SkillSwapProposal> findByIdForUpdate(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<SkillSwapProposal> findByIdAndOwnerIdAndStatusIn(Long id,
                                                              Long ownerId,
                                                              Collection<SkillSwapProposalStatus> statuses);

    boolean existsByAnnounceIdAndStatusInAndIdNot(Long announceId,
                                                  Collection<SkillSwapProposalStatus> statuses,
                                                  Long proposalId);
}
