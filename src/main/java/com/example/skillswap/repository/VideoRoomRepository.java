package com.example.skillswap.repository;

import com.example.skillswap.entity.VideoRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VideoRoomRepository extends JpaRepository<VideoRoom, Long> {

    Optional<VideoRoom> findFirstByChatRoomIdAndActiveTrueOrderByCreatedAtDesc(Long chatRoomId);

    List<VideoRoom> findByChatRoomIdAndActiveTrue(Long chatRoomId);
}
