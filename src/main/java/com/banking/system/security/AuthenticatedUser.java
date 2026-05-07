package com.banking.system.security;

public record AuthenticatedUser(Long userId, String email) {
}
