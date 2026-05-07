package com.banking.system.integration.rest;

import com.banking.system.domain.service.AccountService;
import com.banking.system.integration.models.BalanceResponse;
import com.banking.system.integration.models.DepositRequest;
import com.banking.system.integration.models.DepositResponse;
import com.banking.system.security.AuthenticatedUser;
import com.banking.system.security.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/balance")
    public ResponseEntity<BalanceResponse> getBalance(@CurrentUser AuthenticatedUser user) {
        return ResponseEntity.ok(accountService.getBalance(user.userId()));
    }

    @PostMapping("/deposit")
    public ResponseEntity<DepositResponse> deposit(@CurrentUser AuthenticatedUser user,
                                                   @Valid @RequestBody DepositRequest request) {
        return ResponseEntity.ok(accountService.deposit(user.userId(), request.amount()));
    }
}
