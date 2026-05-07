package com.banking.system.domain.providers;

import com.banking.system.integration.database.JpaAccount;

import java.math.BigDecimal;
import java.util.Optional;

public interface AccountProvider {

    Optional<JpaAccount> findByUserId(Long userId);

    int incrementBalance(Long userId, BigDecimal amount);
}
