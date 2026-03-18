package com.example.skillswap.repository;

import com.example.skillswap.entity.SwapReview;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface SwapReviewRepository extends JpaRepository<SwapReview, Long> {

    boolean existsByProposalIdAndReviewerId(Long proposalId, Long reviewerId);

    @EntityGraph(attributePaths = {"reviewer", "reviewee", "proposal"})
    List<SwapReview> findByProposalIdOrderByCreatedAtAsc(Long proposalId);

    void deleteByProposalIdIn(Collection<Long> proposalIds);
}
