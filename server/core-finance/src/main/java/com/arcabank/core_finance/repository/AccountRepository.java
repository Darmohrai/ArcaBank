package com.arcabank.core_finance.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class AccountRepository {

    private final JdbcTemplate jdbcTemplate;

    public void createAccount(UUID id, UUID userId, String iban, String type, String currency) {
        String sql = "INSERT INTO accounts (id, user_id, iban, type, currency) VALUES (?, ?, ?, ?, ?)";

        jdbcTemplate.update(sql, id, userId, iban, type, currency);
    }
}
