package com.arcabank.core_finance.repository;

import com.arcabank.core_finance.model.Account;
import com.arcabank.core_finance.model.util.AccountStatus;
import com.arcabank.core_finance.model.util.AccountType;
import com.arcabank.core_finance.model.util.Currency;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.dao.EmptyResultDataAccessException;
import com.arcabank.core_finance.dto.CardDto;

import java.util.*;

@Slf4j
@Repository
public class AccountRepository extends BaseRepository<Account> {

    private final static String SP_CREATE_NEW_ACCOUNT_FUNCTION = "sp_create_account_with_card";
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
                                            String cvvHash, String pinHash) {

        Map<String, Object> inParams = new HashMap<>();
        inParams.put("p_user_id", userId);
        inParams.put("p_account_type", type);
        inParams.put("p_currency", currency);
        inParams.put("p_iban", iban);
        inParams.put("p_card_number", cardNumber);
        inParams.put("p_card_holder_name", cardHolderName);
        inParams.put("p_expiration_date", expirationDate);
        inParams.put("p_cvv_hash", cvvHash);
        inParams.put("p_pin_hash", pinHash);

        Map<String, Object> result = executeFunction(SP_CREATE_NEW_ACCOUNT_FUNCTION, inParams);

        Map<String, UUID> ids = new HashMap<>();
        ids.put("account_id", (UUID) result.get("new_account_id"));
        ids.put("cardId", (UUID) result.get("new_card_id"));

        return ids;
    }

    private final RowMapper<CardDto> cardRowMapper = (rs, rowNum) -> CardDto.builder()
        .id(UUID.fromString(rs.getString("id")))
        .accountId(UUID.fromString(rs.getString("account_id")))
        .cardNumber(rs.getString("card_number"))
        .cardHolderName(rs.getString("card_holder_name"))
        .expirationDate(rs.getString("expiration_date"))
        .status(rs.getString("status"))
        .build();

    public CardDto findCardByIdAndUserId(UUID cardId, UUID userId) {
        String sql = "SELECT c.* FROM cards c " +
            "JOIN accounts a ON c.account_id = a.id " +
            "WHERE c.id = ? AND a.user_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, cardRowMapper, cardId, userId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public List<CardDto> findAllCardsByUserId(UUID userId) {
        String sql = "SELECT c.* FROM cards c " +
            "JOIN accounts a ON c.account_id = a.id " +
            "WHERE a.user_id = ?";
        return jdbcTemplate.query(sql, cardRowMapper, userId);
    }

    public Optional<Account> findByIdAndUserId(UUID id, UUID userId) {
        String sql = "SELECT * FROM accounts WHERE id = ? AND user_id = ?";
        return queryList(sql, accountRowMapper, id, userId)
            .stream()
            .findFirst();
    }
}
