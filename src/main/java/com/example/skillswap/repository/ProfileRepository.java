package com.example.skillswap.repository;

import com.example.skillswap.entity.Profil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfileRepository extends JpaRepository<Profil, Long> {

    Optional<Profil> findFirstByUserEmailOrderByIdDesc(String email);

    Optional<Profil> findFirstByUserIdOrderByIdDesc(Long userId);

}
