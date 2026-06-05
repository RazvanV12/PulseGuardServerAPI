package com.personal.pulseguardserverapi.repository;

import com.personal.pulseguardserverapi.entity.Alert;
import com.personal.pulseguardserverapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AlertRepository extends JpaRepository<Alert, UUID> {
    List<Alert> findByUserOrderByCreatedAtDesc(User user);
    List<Alert> findByUserAndAcknowledgedOrderByCreatedAtDesc(User user, boolean acknowledged);
    long countByUser(User user);
}
