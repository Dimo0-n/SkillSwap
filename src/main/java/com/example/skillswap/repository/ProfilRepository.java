package com.example.skillswap.repository;

import com.example.skillswap.entity.Profil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfilRepository extends JpaRepository<Profil, Long> {

    Optional<Profil> findFirstByUserEmailOrderByIdDesc(String email);

}
