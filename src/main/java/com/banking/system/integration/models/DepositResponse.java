package com.banking.system.integration.models;

import java.math.BigDecimal;

public record DepositResponse(
        Long userId,
        BigDecimal depositedAmount,
        BigDecimal balance,
        String currency
) {
}
