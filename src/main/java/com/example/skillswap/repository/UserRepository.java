package com.example.skillswap.repository;

import com.example.skillswap.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>{

    Optional<User> findByEmail(String email);

    User findUserByEmail(String email);

    @Query("""
        SELECT u FROM User u
        JOIN FETCH u.roles
        WHERE u.email = :email
    """)
    Optional<User> findByEmailWithRoles(String email);

}
