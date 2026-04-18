package com.arcabank.auth.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public abstract class BaseRepository<T> {

    protected final JdbcTemplate jdbcTemplate;

    protected List<T> queryList(String sql, RowMapper<T> mapper, Object... params) {
        return jdbcTemplate.query(sql, mapper, params);
    }

    protected T queryOne(String sql, RowMapper<T> mapper, Object... params) {
        return jdbcTemplate.queryForObject(sql, mapper, params);
    }

    protected int update(String sql, Object... params) {
        return jdbcTemplate.update(sql, params);
    }

    protected Map<String, Object> executeFunction(String functionName, Map<String, Object> inParams) {
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
            .withFunctionName(functionName);

        return jdbcCall.execute(inParams);
    }

    protected void callProcedure(String procedureName, Object... params) {
        StringBuilder placeholders = new StringBuilder("(");
        for (int i = 0; i < params.length; i++) {
            placeholders.append("?");
            if (i < params.length - 1) {
                placeholders.append(", ");
            }
        }
        placeholders.append(")");

        String sql = "CALL " + procedureName + placeholders.toString();

        update(sql, params);
    }
}
