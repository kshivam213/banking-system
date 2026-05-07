package com.banking.system.integration.database.repository;

import com.banking.system.integration.database.JpaAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<JpaAccount, Long> {

    Optional<JpaAccount> findByUserId(Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update JpaAccount a set a.balance = a.balance + :amount, a.version = a.version + 1 "
            + "where a.userId = :userId")
    int incrementBalance(@Param("userId") Long userId, @Param("amount") BigDecimal amount);
}
