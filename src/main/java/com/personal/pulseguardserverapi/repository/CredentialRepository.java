package com.personal.pulseguardserverapi.repository;

import com.personal.pulseguardserverapi.entity.Credential;
import com.personal.pulseguardserverapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CredentialRepository extends JpaRepository<Credential, Long> {
    Optional<Credential> findByUser(User user);
}
