package com.personal.pulseguardserverapi.security;

import com.personal.pulseguardserverapi.entity.User;
import com.personal.pulseguardserverapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Called by JwtAuthenticationFilter. Username here is the userId (UUID string)
     * stored as the JWT subject. The password field is left empty because we rely on
     * JWT for authentication, not Spring Security's form-based password check.
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        User user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userId));

        return new org.springframework.security.core.userdetails.User(
                user.getUserId().toString(),
                "",                        // no password needed in JWT filter
                Collections.emptyList()    // no roles needed for this project
        );
    }
}
