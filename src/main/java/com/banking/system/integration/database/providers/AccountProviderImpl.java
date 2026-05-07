package com.banking.system.integration.database.providers;

import com.banking.system.domain.providers.AccountProvider;
import com.banking.system.integration.database.JpaAccount;
import com.banking.system.integration.database.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AccountProviderImpl implements AccountProvider {

    private final AccountRepository accountRepository;

    @Override
    public Optional<JpaAccount> findByUserId(Long userId) {
        return accountRepository.findByUserId(userId);
    }

    @Override
    public int incrementBalance(Long userId, BigDecimal amount) {
        return accountRepository.incrementBalance(userId, amount);
    }
}

