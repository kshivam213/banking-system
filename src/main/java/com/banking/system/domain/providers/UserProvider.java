package com.banking.system.domain.providers;

import com.banking.system.integration.database.JpaUser;

import java.util.Optional;

public interface UserProvider {

    Optional<JpaUser> findByEmail(String email);
}
