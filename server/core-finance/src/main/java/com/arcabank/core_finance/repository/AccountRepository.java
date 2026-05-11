package com.arcabank.core_finance.repository;

import com.arcabank.core_finance.model.Account;
import com.arcabank.core_finance.model.util.AccountStatus;
import com.arcabank.core_finance.model.util.AccountType;
import com.arcabank.core_finance.model.util.Currency;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
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

    public List<Account> findAllByUserId(UUID userId) {
        String sql = "SELECT * FROM " + SP_GET_ALL_ACCOUNTS_BY_USER_ID + "(?)";
        return queryList(sql, accountRowMapper, userId);
    }

    public Map<String, UUID> callCreateAccountWithCard(UUID userId, String type, String currency,
                                            String iban, String cardNumber,
                                            String cardHolderName, String expirationDate,
                                            String cvvHash) {

        Map<String, Object> inParams = new HashMap<>();
        inParams.put("p_user_id", userId);
        inParams.put("p_account_type", type);
        inParams.put("p_currency", currency);
        inParams.put("p_iban", iban);
        inParams.put("p_card_number", cardNumber);
        inParams.put("p_card_holder_name", cardHolderName);
        inParams.put("p_expiration_date", expirationDate);
        inParams.put("p_cvv_hash", cvvHash);

        Map<String, Object> result = executeFunction(SP_CREATE_NEW_ACCOUNT_FUNCTION, inParams);

        Map<String, UUID> ids = new HashMap<>();
        ids.put("account_id", (UUID) result.get("new_account_id"));
        ids.put("cardId", (UUID) result.get("new_card_id"));

        return ids;
    }
}
