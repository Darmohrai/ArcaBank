package com.arcabank.core_finance.repository;

import com.arcabank.core_finance.model.EscrowTransaction;
import com.arcabank.core_finance.model.util.EscrowStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class EscrowTransactionRepository extends BaseRepository<EscrowTransaction> {

    public EscrowTransactionRepository(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    public boolean existsByChestIdAndStatus(UUID chestId, String status) {
        String sql = "SELECT count(*) FROM escrow_transactions WHERE chest_id = ? AND status = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, chestId, status);
        return count > 0;
    }

    private final RowMapper<EscrowTransaction> escrowRowMapper = (rs, rowNum) -> EscrowTransaction.builder()
        .id(UUID.fromString(rs.getString("id")))
        .chestId(UUID.fromString(rs.getString("chest_id")))
        .initiatorId(UUID.fromString(rs.getString("initiator_id")))
        .amount(rs.getBigDecimal("amount"))
        .destinationAccountId(UUID.fromString(rs.getString("destination_account")))
        .purpose(rs.getString("purpose"))
        .status(EscrowStatus.valueOf(rs.getString("status")))
        .build();

    public Optional<EscrowTransaction> findByIdForUpdate(UUID id) {
        String sql = "SELECT * FROM escrow_transactions WHERE id = ? FOR UPDATE";
        return queryList(sql, escrowRowMapper, id).stream().findFirst();
    }

    public void updateStatus(UUID id, String status) {
        String sql = "UPDATE escrow_transactions SET status = ? WHERE id = ?";
        update(sql, status, id);
    }

    public void save(EscrowTransaction transaction) {
        String sql = "INSERT INTO escrow_transactions (id, chest_id, initiator_id, amount, destination_account, purpose, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        update(sql, transaction.getId(), transaction.getChestId(), transaction.getInitiatorId(),
            transaction.getAmount(), transaction.getDestinationAccountId(), transaction.getPurpose(), transaction.getStatus().name());
    }

    public Optional<EscrowTransaction> findPendingByChestId(UUID chestId) {
        String sql = "SELECT * FROM escrow_transactions WHERE chest_id = ? AND status = 'PENDING'";
        return queryList(sql, escrowRowMapper, chestId).stream().findFirst();
    }

    public List<EscrowTransaction> findAllByChestId(UUID chestId) {
        String sql = "SELECT * FROM escrow_transactions WHERE chest_id = ? ORDER BY created_at DESC";
        return queryList(sql, escrowRowMapper, chestId);
    }
}
