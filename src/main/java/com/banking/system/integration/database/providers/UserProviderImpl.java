package com.banking.system.integration.database.providers;

import com.banking.system.domain.providers.UserProvider;
import com.banking.system.integration.database.JpaUser;
import com.banking.system.integration.database.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserProviderImpl implements UserProvider {

    private final UserRepository userRepository;

    @Override
    public Optional<JpaUser> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}

