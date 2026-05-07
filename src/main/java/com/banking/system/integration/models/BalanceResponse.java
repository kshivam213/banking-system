package com.banking.system.integration.models;

import java.math.BigDecimal;

public record BalanceResponse(
        Long userId,
        BigDecimal balance,
        String currency
) {
}
