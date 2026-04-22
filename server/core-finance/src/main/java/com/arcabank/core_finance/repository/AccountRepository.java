package com.arcabank.core_finance.repository;

import com.arcabank.core_finance.model.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Repository
public class AccountRepository extends BaseRepository<Account> {

    private final String SP_CREATE_NEW_ACCOUNT_FUNCTION = "sp_create_new_account";

    public AccountRepository(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    public UUID createAccount(Account account) {
        Map<String, Object> inParams = new HashMap<>();
        inParams.put("p_user_id", account.getUserId());
        inParams.put("p_iban", account.getIban());

        inParams.put("p_type", account.getType().name());
        inParams.put("p_currency", account.getCurrency().name());

        Map<String, Object> result = executeFunction(SP_CREATE_NEW_ACCOUNT_FUNCTION, inParams);

        return (UUID) result.get("returnvalue");
    }
}
