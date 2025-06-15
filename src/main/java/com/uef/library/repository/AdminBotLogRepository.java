package com.uef.library.repository;

import com.uef.library.model.AdminBotLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdminBotLogRepository extends JpaRepository<AdminBotLog, Long> {
    List<AdminBotLog> findTop50ByOrderByCreatedAtDesc();
}