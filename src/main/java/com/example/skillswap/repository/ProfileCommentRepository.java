package com.example.skillswap.repository;

import com.example.skillswap.entity.ProfileComment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProfileCommentRepository extends JpaRepository<ProfileComment, Long> {

    @EntityGraph(attributePaths = "author")
    List<ProfileComment> findByProfileOwnerIdOrderByCreatedAtDescIdDesc(Long profileOwnerId, Pageable pageable);

    long countByProfileOwnerId(Long profileOwnerId);

    @EntityGraph(attributePaths = {"author", "profileOwner"})
    @Query("""
        select comment
        from ProfileComment comment
        where comment.id = :commentId
          and comment.profileOwner.id = :profileOwnerId
    """)
    Optional<ProfileComment> findDetailedByIdAndProfileOwnerId(Long commentId, Long profileOwnerId);

    @Query("""
        select comment.content
        from ProfileComment comment
        where comment.profileOwner.id = :profileOwnerId
        order by comment.createdAt asc, comment.id asc
    """)
    List<String> findContentsByProfileOwnerId(Long profileOwnerId);
}
