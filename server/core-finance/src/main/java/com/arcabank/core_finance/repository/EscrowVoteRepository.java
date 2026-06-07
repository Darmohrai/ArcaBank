package com.arcabank.core_finance.repository;

import com.arcabank.core_finance.model.EscrowTransaction;
import com.arcabank.core_finance.model.EscrowVote;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public class EscrowVoteRepository extends BaseRepository<EscrowTransaction> {
    public EscrowVoteRepository(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    public void save(EscrowVote vote) {
        String sql = "INSERT INTO escrow_votes (id, escrow_transaction_id, user_id, decision, created_at) VALUES (?, ?, ?, ?, ?)";
        update(sql, vote.getId(), vote.getEscrowTransactionId(), vote.getUserId(), vote.getDecision(), vote.getCreatedAt() != null ? vote.getCreatedAt() : LocalDateTime.now());
    }

    public boolean existsByEscrowTransactionIdAndUserId(UUID escrowTransactionId, UUID userId) {
        String sql = "SELECT count(*) FROM escrow_votes WHERE escrow_transaction_id = ? AND user_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, escrowTransactionId, userId);
        return count != null && count > 0;
    }

    public int countApprovals(UUID escrowTransactionId) {
        String sql = "SELECT count(*) FROM escrow_votes WHERE escrow_transaction_id = ? AND decision = 'APPROVED'";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, escrowTransactionId);
        return count != null ? count : 0;
    }
}
