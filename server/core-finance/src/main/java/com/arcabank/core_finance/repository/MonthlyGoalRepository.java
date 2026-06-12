package com.arcabank.core_finance.repository;

import com.arcabank.core_finance.model.MonthlyGoal;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
public class MonthlyGoalRepository extends BaseRepository<MonthlyGoal> {

    public MonthlyGoalRepository(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    private final RowMapper<MonthlyGoal> rowMapper = (rs, rowNum) -> MonthlyGoal.builder()
        .id(UUID.fromString(rs.getString("id")))
        .userId(UUID.fromString(rs.getString("user_id")))
        .year(rs.getInt("goal_year"))
        .month(rs.getInt("goal_month"))
        .targetAmount(rs.getBigDecimal("target_amount"))
        .createdAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null)
        .updatedAt(rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null)
        .build();

    public UUID upsertGoal(UUID userId, int year, int month, BigDecimal amount) {
        Map<String, Object> inParams = new HashMap<>();
        inParams.put("p_user_id", userId);
        inParams.put("p_year", year);
        inParams.put("p_month", month);
        inParams.put("p_amount", amount);

        Map<String, Object> result = executeFunction("sp_upsert_monthly_goal", inParams);
        return (UUID) result.get("returnvalue");
    }

    public Optional<MonthlyGoal> findByUserIdAndYearAndMonth(UUID userId, int year, int month) {
        String sql = "SELECT * FROM monthly_goals WHERE user_id = ? AND goal_year = ? AND goal_month = ?";
        List<MonthlyGoal> result = queryList(sql, rowMapper, userId, year, month);
        return result.stream().findFirst();
    }
}
