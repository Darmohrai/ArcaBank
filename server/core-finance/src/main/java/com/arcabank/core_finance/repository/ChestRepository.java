package com.arcabank.core_finance.repository;

import com.arcabank.core_finance.model.Chest;
import com.arcabank.core_finance.model.util.ChestStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ChestRepository extends BaseRepository<Chest> {

    public ChestRepository(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    private final RowMapper<Chest> chestRowMapper = (rs, rowMapper) -> Chest.builder()
        .id(UUID.fromString(rs.getString("id")))
        .accountId(rs.getString("account_id") != null ? UUID.fromString(rs.getString("account_id")) : null)
        .name(rs.getString("name"))
        .description(rs.getString("description"))
        .targetAmount(rs.getBigDecimal("target_amount"))
        .balance(rs.getBigDecimal("balance"))
        .frozenBalance(rs.getBigDecimal("frozen_balance"))
        .status(ChestStatus.valueOf(rs.getString("status")))
        .createdAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null)
        .build();

    public UUID createChest(Chest chest) {
        UUID newChestId = UUID.randomUUID();
        String sql = "INSERT INTO chests (id, account_id, name, description, target_amount, status) VALUES (?, ?, ?, ?, ?, ?)";

        update(sql, newChestId, chest.getAccountId(), chest.getName(), chest.getDescription(), chest.getTargetAmount(), chest.getStatus().name());

        return newChestId;
    }

    public Optional<Chest> findChestById(UUID id) {
        String sql = "SELECT * FROM chests WHERE id = ?";
        List<Chest> result = queryList(sql, chestRowMapper, id);
        return result.stream().findFirst();
    }

    public void processChestDeposit(UUID accountId, UUID chestId, BigDecimal amount) {
        callProcedure("sp_process_chest_deposit", accountId, chestId, amount);
    }

    public void freezeFunds(UUID chestId, BigDecimal amount) {
        String sql = "UPDATE chests SET balance = balance - ?, frozen_balance = COALESCE(frozen_balance, 0) + ? WHERE id = ?";
        update(sql, amount, amount, chestId);
    }
}
