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

    public java.util.Map<UUID, Integer> countApprovalsForEscrows(java.util.List<UUID> escrowIds) {
        if (escrowIds == null || escrowIds.isEmpty()) {
            return java.util.Collections.emptyMap();
        }

        String inSql = String.join(",", java.util.Collections.nCopies(escrowIds.size(), "?"));
        String sql = "SELECT escrow_transaction_id, COUNT(*) as cnt " +
            "FROM escrow_votes " +
            "WHERE decision = 'APPROVED' AND escrow_transaction_id IN (" + inSql + ") " +
            "GROUP BY escrow_transaction_id";

        return jdbcTemplate.query(sql, rs -> {
            java.util.Map<UUID, Integer> map = new java.util.HashMap<>();
            while (rs.next()) {
                map.put(UUID.fromString(rs.getString("escrow_transaction_id")), rs.getInt("cnt"));
            }
            return map;
        }, escrowIds.toArray());
    }

    public java.util.Set<UUID> findEscrowsVotedByUser(java.util.List<UUID> escrowIds, UUID userId) {
        if (escrowIds == null || escrowIds.isEmpty()) {
            return java.util.Collections.emptySet();
        }

        String inSql = String.join(",", java.util.Collections.nCopies(escrowIds.size(), "?"));
        String sql = "SELECT escrow_transaction_id FROM escrow_votes " +
            "WHERE user_id = ? AND escrow_transaction_id IN (" + inSql + ")";

        java.util.List<Object> params = new java.util.ArrayList<>();
        params.add(userId);
        params.addAll(escrowIds);

        return jdbcTemplate.query(sql, rs -> {
            java.util.Set<UUID> set = new java.util.HashSet<>();
            while (rs.next()) {
                set.add(UUID.fromString(rs.getString("escrow_transaction_id")));
            }
            return set;
        }, params.toArray());
    }
}
