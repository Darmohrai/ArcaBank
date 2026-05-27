package com.arcabank.core_finance.repository;

import com.arcabank.core_finance.model.ChestMember;
import com.arcabank.core_finance.model.util.ChestMemberRole;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public class ChestMemberRepository extends BaseRepository<ChestMember> {

    public ChestMemberRepository(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    public void addChestMember(UUID chestId, UUID userId, ChestMemberRole role, LocalDateTime joinedAt) {
        String sql = "INSERT INTO chest_members (chest_id, user_id, role, joined_at) VALUES (?, ?, ?, ?)";
        update(sql, chestId, userId, role.name(), joinedAt);
    }
}
