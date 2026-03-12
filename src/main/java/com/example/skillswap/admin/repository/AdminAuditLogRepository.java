package com.example.skillswap.admin.repository;

import com.example.skillswap.admin.entity.AdminAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdminAuditLogRepository extends JpaRepository<AdminAuditLog, Long> {

    List<AdminAuditLog> findTop12ByOrderByCreatedAtDesc();
}