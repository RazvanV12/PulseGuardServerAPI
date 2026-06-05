package com.personal.pulseguardserverapi.repository;

import com.personal.pulseguardserverapi.entity.Session;
import com.personal.pulseguardserverapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SessionRepository extends JpaRepository<Session, UUID> {
    List<Session> findByUserOrderByStartedAtDesc(User user);
}
