package com.example.skillswap.entity;

import com.example.skillswap.enums.AuthProvider;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String fullName;

    @Email
    @NotBlank
    @Column(unique = true)
    private String email;

    @NotBlank(message = "{register.validation.password.required}")
    @Size(min = 10, max = 72, message = "{register.validation.password.size}")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).+$",
            message = "{register.validation.password.complexity}"
    )
    private String password;

    @Enumerated(EnumType.STRING)
    private AuthProvider provider;

    private LocalDateTime registerData;

    private LocalDateTime lastLoginAt;

    @Column(nullable = false)
    private Boolean online = false;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean profileCompleted = false;

    private LocalDateTime lastActivityAt;

    private LocalDateTime lastSeenAt;

    @Column(nullable = false)
    private boolean suspended = false;

    @Column(nullable = false)
    private boolean banned = false;

    @Column(nullable = false)
    private boolean deleted = false;

    @Column(length = 64)
    private String timeZoneId;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "users_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();
}

