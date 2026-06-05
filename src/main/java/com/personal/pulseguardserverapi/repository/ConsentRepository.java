package com.personal.pulseguardserverapi.repository;

import com.personal.pulseguardserverapi.entity.Consent;
import com.personal.pulseguardserverapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ConsentRepository extends JpaRepository<Consent, UUID> {
    List<Consent> findByUser(User user);
}
