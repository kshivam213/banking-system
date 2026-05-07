package com.banking.system.domain.service;

import com.banking.system.domain.exception.AccountNotFoundException;
import com.banking.system.domain.exception.InvalidAmountException;
import com.banking.system.domain.providers.AccountProvider;
import com.banking.system.integration.database.JpaAccount;
import com.banking.system.integration.models.BalanceResponse;
import com.banking.system.integration.models.DepositResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountProvider accountProvider;

    @Transactional(readOnly = true)
    public BalanceResponse getBalance(Long userId) {
        JpaAccount account = getAccount(userId);

        return new BalanceResponse(
                userId,
                account.getBalance(),
                account.getCurrency()
        );
    }

    @Transactional
    public DepositResponse deposit(Long userId, BigDecimal amount) {
        validateAmount(amount);

        int updated = accountProvider.incrementBalance(userId, amount);
        if (updated == 0) {
            throw new AccountNotFoundException("Account not found for user " + userId);
        }

        JpaAccount account = getAccount(userId);
        return new DepositResponse(
                userId,
                amount,
                account.getBalance(),
                account.getCurrency()
        );
    }

    private JpaAccount getAccount(Long userId) {
        return accountProvider.findByUserId(userId)
                .orElseThrow(() ->
                        new AccountNotFoundException(
                                "Account not found for user " + userId
                        )
                );
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new InvalidAmountException(
                    "Deposit amount must be positive"
            );
        }
    }
}