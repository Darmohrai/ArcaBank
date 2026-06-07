package com.arcabank.core_finance.repository;

import com.arcabank.core_finance.model.ChestMember;
import com.arcabank.core_finance.model.util.ChestMemberRole;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ChestMemberRepository extends BaseRepository<ChestMember> {

    public ChestMemberRepository(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    private final RowMapper<ChestMember> chestMemberRowMapper = (rs, rowNum) -> ChestMember.builder()
        .chestId(UUID.fromString(rs.getString("chest_id")))
        .userId(UUID.fromString(rs.getString("user_id")))
        .role(ChestMemberRole.valueOf(rs.getString("role")))
        .joinedAt(rs.getTimestamp("joined_at") != null ? rs.getTimestamp("joined_at").toLocalDateTime() : null)
        .build();

    public void addChestMember(UUID chestId, UUID userId, ChestMemberRole role, LocalDateTime joinedAt) {
        String sql = "INSERT INTO chest_members (chest_id, user_id, role, joined_at) VALUES (?, ?, ?, ?)";
        update(sql, chestId, userId, role.name(), joinedAt);
    }

    public Optional<ChestMember> findByChestIdAndUserId(UUID chestId, UUID userId) {
        String sql = "SELECT * FROM chest_members WHERE chest_id = ? AND user_id = ?";

        List<ChestMember> result = queryList(sql, chestMemberRowMapper, chestId, userId);

        return result.stream().findFirst();
    }

    public List<ChestMember> findByChestIdAndRole(UUID chestId, ChestMemberRole role) {
        String sql = "SELECT * FROM chest_members WHERE chest_id = ? AND role = ?";
        return queryList(sql, chestMemberRowMapper, chestId, role.name());
    }

    public List<ChestMember> findByChestId(UUID chestId) {
        String sql = "SELECT * FROM chest_members WHERE chest_id = ?";
        return queryList(sql, chestMemberRowMapper, chestId);
    }
}
