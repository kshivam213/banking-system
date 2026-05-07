package com.banking.system.security;

import com.banking.system.domain.exception.InvalidTokenException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRET = "test-secret-that-is-at-least-32-bytes-long!!";

    private final JwtService jwtService = new JwtService(new JwtProperties(SECRET, 60));

    @Test
    void issueAndParse_roundTripReturnsSameUser() {
        JwtService.IssuedToken issued = jwtService.issueToken(42L, "demo@bank.local");

        AuthenticatedUser user = jwtService.parseAndValidate(issued.token());

        assertThat(user.userId()).isEqualTo(42L);
        assertThat(user.email()).isEqualTo("demo@bank.local");
        assertThat(issued.expiresAt()).isNotNull();
    }

    @Test
    void parse_throwsInvalidTokenForGarbage() {
        assertThatThrownBy(() -> jwtService.parseAndValidate("not-a-real-token"))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void parse_throwsInvalidTokenWhenSignedWithDifferentSecret() {
        JwtService other = new JwtService(
                new JwtProperties("another-secret-that-is-also-32-bytes-long!!!", 60));
        String foreignToken = other.issueToken(1L, "x@y.z").token();

        assertThatThrownBy(() -> jwtService.parseAndValidate(foreignToken))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void constructor_rejectsShortSecret() {
        assertThatThrownBy(() -> new JwtService(new JwtProperties("too-short", 60)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("at least 32 bytes");
    }
}

