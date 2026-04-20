package com.arcabank.auth.repository;

import com.arcabank.auth.model.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.util.UUID;
import java.util.List;
import java.util.Optional;

@Repository
public class UserRepository extends BaseRepository<User> {

    private static final String SP_SYNC_USER = "sp_sync_user";

    private final RowMapper<User> userMapper = (rs, rowNum) -> User.builder()
        .id(rs.getObject("id", UUID.class))
        .email(rs.getString("email"))
        .firstName(rs.getString("first_name"))
        .lastName(rs.getString("last_name"))
        .passportId(rs.getString("passport_id"))
        .phoneNumber(rs.getString("phone_number"))
        .status(rs.getString("status"))
        .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
        .build();

    public UserRepository(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    public List<User> findAll() {
        return queryList("SELECT * FROM users", userMapper);
    }

    public Optional<User> findById(UUID id) {
        return queryList("SELECT * FROM users WHERE id = ?", userMapper, id)
            .stream()
            .findFirst();
    }

    public Optional<User> findByEmail(String email) {
        return queryList("SELECT * FROM users WHERE email = ?", userMapper, email)
            .stream()
            .findFirst();
    }

    public void syncUser(UUID id, String email, String firstName, String lastName, String passportId, String phoneNumber) {
        callProcedure(SP_SYNC_USER, id, email, firstName, lastName, passportId, phoneNumber);
    }
}
