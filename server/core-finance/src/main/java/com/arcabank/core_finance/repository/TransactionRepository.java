package com.arcabank.core_finance.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Repository
public class TransactionRepository extends BaseRepository<Object> {

    public TransactionRepository(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    public void createTransaction(UUID senderId, UUID receiverId, BigDecimal amount, String currency, String status) {
        String sql = "INSERT INTO transactions (id, sender_account_id, receiver_account_id, amount, currency, status, created_at) " +
            "VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";

        UUID transactionId = UUID.randomUUID();

        update(sql, transactionId, senderId, receiverId, amount, currency, status);
    }
}
