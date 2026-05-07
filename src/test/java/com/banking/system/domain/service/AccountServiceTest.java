package com.banking.system.domain.service;

import com.banking.system.domain.exception.AccountNotFoundException;
import com.banking.system.domain.exception.InvalidAmountException;
import com.banking.system.domain.providers.AccountProvider;
import com.banking.system.integration.database.JpaAccount;
import com.banking.system.integration.models.BalanceResponse;
import com.banking.system.integration.models.DepositResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    private static final Long USER_ID = 42L;

    @Mock
    private AccountProvider accountProvider;

    @InjectMocks
    private AccountService accountService;

    @Test
    void getBalance_returnsAccountBalance() {
        JpaAccount account = JpaAccount.builder()
                .userId(USER_ID)
                .balance(new BigDecimal("125.50"))
                .currency("USD")
                .build();
        when(accountProvider.findByUserId(USER_ID)).thenReturn(Optional.of(account));

        BalanceResponse response = accountService.getBalance(USER_ID);

        assertThat(response.userId()).isEqualTo(USER_ID);
        assertThat(response.balance()).isEqualByComparingTo("125.50");
        assertThat(response.currency()).isEqualTo("USD");
    }

    @Test
    void getBalance_throwsWhenAccountMissing() {
        when(accountProvider.findByUserId(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.getBalance(USER_ID))
                .isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    void deposit_incrementsBalanceAndReturnsNewTotal() {
        BigDecimal amount = new BigDecimal("50.00");
        JpaAccount updated = JpaAccount.builder()
                .userId(USER_ID)
                .balance(new BigDecimal("175.50"))
                .currency("USD")
                .build();
        when(accountProvider.incrementBalance(USER_ID, amount)).thenReturn(1);
        when(accountProvider.findByUserId(USER_ID)).thenReturn(Optional.of(updated));

        DepositResponse response = accountService.deposit(USER_ID, amount);

        assertThat(response.userId()).isEqualTo(USER_ID);
        assertThat(response.depositedAmount()).isEqualByComparingTo("50.00");
        assertThat(response.balance()).isEqualByComparingTo("175.50");
        assertThat(response.currency()).isEqualTo("USD");
        verify(accountProvider).incrementBalance(USER_ID, amount);
    }

    @Test
    void deposit_throwsWhenNoRowsUpdated() {
        BigDecimal amount = new BigDecimal("10.00");
        when(accountProvider.incrementBalance(USER_ID, amount)).thenReturn(0);

        assertThatThrownBy(() -> accountService.deposit(USER_ID, amount))
                .isInstanceOf(AccountNotFoundException.class);
        verify(accountProvider, never()).findByUserId(eq(USER_ID));
    }

    @Test
    void deposit_rejectsZeroOrNegativeAmounts() {
        assertThatThrownBy(() -> accountService.deposit(USER_ID, BigDecimal.ZERO))
                .isInstanceOf(InvalidAmountException.class);
        assertThatThrownBy(() -> accountService.deposit(USER_ID, new BigDecimal("-1.00")))
                .isInstanceOf(InvalidAmountException.class);
        assertThatThrownBy(() -> accountService.deposit(USER_ID, null))
                .isInstanceOf(InvalidAmountException.class);

        verify(accountProvider, never()).incrementBalance(eq(USER_ID), org.mockito.ArgumentMatchers.any());
    }
}

