package com.arcabank.core_finance.repository;

import com.arcabank.core_finance.model.Account;
import com.arcabank.core_finance.model.util.AccountStatus;
import com.arcabank.core_finance.model.util.AccountType;
import com.arcabank.core_finance.model.util.Currency;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Repository
public class AccountRepository extends BaseRepository<Account> {

    private final static String SP_CREATE_NEW_ACCOUNT_FUNCTION = "sp_create_new_account";
    private final static String SP_GET_ALL_ACCOUNTS_BY_USER_ID = "sp_get_all_accounts_by_user_id";

    public AccountRepository(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    private final RowMapper<Account> accountRowMapper = (rs, rowNum) -> Account.builder()
        .id(UUID.fromString(rs.getString("id")))
        .userId(UUID.fromString(rs.getString("user_id")))
        .iban(rs.getString("iban"))
        .type(AccountType.valueOf(rs.getString("type")))
        .currency(Currency.valueOf(rs.getString("currency")))
        .balance(rs.getBigDecimal("balance"))
        .status(AccountStatus.valueOf(rs.getString("status")))
        .createdAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null)
        .build();

    public UUID createAccount(Account account) {
        Map<String, Object> inParams = new HashMap<>();
        inParams.put("p_user_id", account.getUserId());
        inParams.put("p_iban", account.getIban());

        inParams.put("p_type", account.getType().name());
        inParams.put("p_currency", account.getCurrency().name());

        Map<String, Object> result = executeFunction(SP_CREATE_NEW_ACCOUNT_FUNCTION, inParams);

        return (UUID) result.get("returnvalue");
    }

    public List<Account> findAllByUserId(UUID userId) {
        String sql = "SELECT * FROM " + SP_GET_ALL_ACCOUNTS_BY_USER_ID + "(?)";
        return queryList(sql, accountRowMapper, userId);
    }
}
