package com.arcabank.core_finance.repository;

import com.arcabank.core_finance.dto.MonthlyStatsDto;
import com.arcabank.core_finance.dto.TransactionDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Repository
public class TransactionRepository extends BaseRepository<TransactionDto> {

    public TransactionRepository(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    private final RowMapper<TransactionDto> transactionRowMapper = (rs, rowNum) -> TransactionDto.builder()
        .id(UUID.fromString(rs.getString("id")))
        .senderAccountId(UUID.fromString(rs.getString("sender_account_id")))
        .receiverAccountId(rs.getString("receiver_account_id") != null ? UUID.fromString(rs.getString("receiver_account_id")) : null)
        .amount(rs.getBigDecimal("amount"))
        .currency(rs.getString("currency"))
        .status(rs.getString("status"))
        .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
        .build();

    public void createTransaction(UUID senderId, UUID receiverId, BigDecimal amount, String currency, String status) {
        String sql = "INSERT INTO transactions (id, sender_account_id, receiver_account_id, amount, currency, status, created_at) " +
            "VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";

        UUID transactionId = UUID.randomUUID();

        update(sql, transactionId, senderId, receiverId, amount, currency, status);
    }

    public List<TransactionDto> findTransactionsByAccountId(UUID accountId, int limit, int offset) {
        String sql = """
            SELECT * FROM transactions
            WHERE sender_account_id = ? OR receiver_account_id = ?
            ORDER BY created_at DESC
            LIMIT ? OFFSET ?
            """;
        return queryList(sql, transactionRowMapper, accountId, accountId, limit, offset);
    }

    public long countTransactionsByAccountId(UUID accountId) {
        String sql = "SELECT COUNT(*) FROM transactions WHERE sender_account_id = ? OR receiver_account_id = ?";
        return jdbcTemplate.queryForObject(sql, Long.class, accountId, accountId);
    }

    public List<TransactionDto> findAllTransactionsByUserId(UUID userId, int limit, int offset) {
        String sql = """
            SELECT DISTINCT t.* FROM transactions t
            LEFT JOIN accounts sender_acc ON t.sender_account_id = sender_acc.id
            LEFT JOIN accounts receiver_acc ON t.receiver_account_id = receiver_acc.id
            WHERE sender_acc.user_id = ? OR receiver_acc.user_id = ?
            ORDER BY t.created_at DESC
            LIMIT ? OFFSET ?
            """;
        return queryList(sql, transactionRowMapper, userId, userId, limit, offset);
    }

    public long countAllTransactionsByUserId(UUID userId) {
        String sql = """
            SELECT COUNT(DISTINCT t.id) FROM transactions t
            LEFT JOIN accounts sender_acc ON t.sender_account_id = sender_acc.id
            LEFT JOIN accounts receiver_acc ON t.receiver_account_id = receiver_acc.id
            WHERE sender_acc.user_id = ? OR receiver_acc.user_id = ?
            """;
        return jdbcTemplate.queryForObject(sql, Long.class, userId, userId);
    }

    public List<MonthlyStatsDto> getMonthlyStats(UUID userId) {
        String sql = """
                SELECT
                    EXTRACT(MONTH FROM t.created_at) as month,
                    SUM(CASE WHEN r.user_id = ? THEN t.amount ELSE 0 END) as income,
                    SUM(CASE WHEN s.user_id = ? THEN t.amount ELSE 0 END) as expense
                FROM transactions t
                JOIN accounts s ON t.sender_account_id = s.id
                LEFT JOIN accounts r ON t.receiver_account_id = r.id
                WHERE (s.user_id = ? OR r.user_id = ?)
                GROUP BY EXTRACT(MONTH FROM t.created_at)
                ORDER BY month;
            """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> new MonthlyStatsDto(
            rs.getInt("month"),
            rs.getDouble("income"),
            rs.getDouble("expense")
        ), userId, userId, userId, userId);
    }
}
