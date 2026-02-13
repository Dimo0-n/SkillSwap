package com.example.skillswap.repository;

import com.example.skillswap.entity.Announce;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnnounceRepository extends JpaRepository<Announce, Long> {

    @Query("SELECT a FROM Announce a WHERE a.user.id = :userId")
    List<Announce> getAnnouncesListByEmail(@Param("userId") Long userId);

    boolean existsByIdAndUserId(Long id, Long user_id);

    List<Announce> findAllByOrderByIdDesc();
}
