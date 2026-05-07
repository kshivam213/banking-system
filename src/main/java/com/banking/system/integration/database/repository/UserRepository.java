package com.banking.system.integration.database.repository;

import com.banking.system.integration.database.JpaUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<JpaUser, Long> {

    Optional<JpaUser> findByEmail(String email);

    boolean existsByEmail(String email);
}
