package com.banking.system.domain.service;

import com.banking.system.domain.providers.UserProvider;
import com.banking.system.integration.database.JpaUser;
import com.banking.system.integration.models.LoginRequest;
import com.banking.system.integration.models.LoginResponse;
import com.banking.system.domain.exception.InvalidCredentialsException;
import com.banking.system.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String GENERIC_INVALID_CREDENTIALS = "Invalid email or PIN";

    private final UserProvider userProvider;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        JpaUser user = userProvider.findByEmail(request.email())
                .orElseThrow(() -> new InvalidCredentialsException(GENERIC_INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.pin(), user.getPinHash())) {
            throw new InvalidCredentialsException(GENERIC_INVALID_CREDENTIALS);
        }

        JwtService.IssuedToken issued = jwtService.issueToken(user.getId(), user.getEmail());
        log.info("User {} logged in successfully", user.getId());
        return new LoginResponse(issued.token(), "Bearer", issued.expiresAt(), user.getId(), user.getEmail());
    }
}
