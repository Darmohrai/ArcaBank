package com.arcabank.core_finance.repository;

import com.arcabank.core_finance.model.EscrowTransaction;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

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

    public void save(EscrowTransaction transaction) {
        String sql = "INSERT INTO escrow_transactions (id, chest_id, initiator_id, amount, destination_account, purpose, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        update(sql, transaction.getId(), transaction.getChestId(), transaction.getInitiatorId(),
            transaction.getAmount(), transaction.getDestinationAccountId(), transaction.getPurpose(), transaction.getStatus().name());
    }
}
