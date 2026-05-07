package com.banking.system.domain.service;

import com.banking.system.domain.exception.InvalidCredentialsException;
import com.banking.system.domain.providers.UserProvider;
import com.banking.system.integration.database.JpaUser;
import com.banking.system.integration.models.LoginRequest;
import com.banking.system.integration.models.LoginResponse;
import com.banking.system.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserProvider userProvider;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private JpaUser user;

    @BeforeEach
    void setUp() {
        user = JpaUser.builder()
                .id(1L)
                .email("demo@bank.local")
                .pinHash("hashed-pin")
                .build();
    }

    @Test
    void login_returnsTokenWhenCredentialsAreValid() {
        Instant expiresAt = Instant.parse("2026-05-07T13:00:00Z");
        when(userProvider.findByEmail("demo@bank.local")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("1234", "hashed-pin")).thenReturn(true);
        when(jwtService.issueToken(1L, "demo@bank.local"))
                .thenReturn(new JwtService.IssuedToken("jwt-token", expiresAt));

        LoginResponse response = authService.login(new LoginRequest("demo@bank.local", "1234"));

        assertThat(response.accessToken()).isEqualTo("jwt-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresAt()).isEqualTo(expiresAt);
        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo("demo@bank.local");
    }

    @Test
    void login_throwsInvalidCredentialsWhenUserNotFound() {
        when(userProvider.findByEmail("missing@bank.local")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest("missing@bank.local", "1234")))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid email or PIN");

        verify(passwordEncoder, never()).matches(any(), any());
        verify(jwtService, never()).issueToken(any(), any());
    }

    @Test
    void login_throwsInvalidCredentialsWhenPinDoesNotMatch() {
        when(userProvider.findByEmail("demo@bank.local")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("0000", "hashed-pin")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest("demo@bank.local", "0000")))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid email or PIN");

        verify(jwtService, never()).issueToken(eq(1L), any());
    }
}

