package com.arcabank.core_finance.repository;

import com.arcabank.core_finance.model.Chest;
import com.arcabank.core_finance.model.util.ChestStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class ChestRepository extends BaseRepository<Chest> {

    public ChestRepository(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    private final RowMapper<Chest> chestRowMapper = (rs, rowMapper) -> Chest.builder()
        .id(UUID.fromString(rs.getString("id")))
        .name(rs.getString("name"))
        .description(rs.getString("description"))
        .targetAmount(rs.getBigDecimal("target_amount"))
        .status(ChestStatus.valueOf(rs.getString("status")))
        .createdAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null)
        .build();

    public UUID createChest(Chest chest) {
        UUID newChestId = UUID.randomUUID();
        String sql = "INSERT INTO chests (id, name, description, target_amount, status) VALUES (?, ?, ?, ?, ?)";

        update(sql, newChestId, chest.getName(), chest.getDescription(), chest.getTargetAmount(), chest.getStatus().name());

        return newChestId;
    }
}
