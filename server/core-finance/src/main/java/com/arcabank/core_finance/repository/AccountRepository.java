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

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Repository
public class AccountRepository extends BaseRepository<Account> {

    private final static String SP_CREATE_NEW_ACCOUNT_WITH_CARD = "sp_create_account_with_card";
    private final static String SP_GET_ALL_ACCOUNTS_BY_USER_ID = "sp_get_all_accounts_by_user_id";
    private final static String SP_CREATE_NEW_ACCOUNT = "sp_create_new_account";
    private final static String SP_CREATE_CARD = "fn_create_card";

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

        Map<String, Object> result = executeFunction(SP_CREATE_NEW_ACCOUNT_WITH_CARD, inParams);

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

    public UUID createJustAccount(Account account) {
        Map<String, Object> inParams = new HashMap<>();
        inParams.put("p_user_id", account.getUserId());
        inParams.put("p_iban", account.getIban());
        inParams.put("p_type", account.getType().name());
        inParams.put("p_currency", account.getCurrency().name());

        Map<String, Object> result = executeFunction(SP_CREATE_NEW_ACCOUNT, inParams);
        return (UUID) result.get("returnvalue");
    }

    public UUID createJustCard(UUID accountId, String pan, String holder, String exp, String cvv, String pin) {
        Map<String, Object> inParams = new HashMap<>();
        inParams.put("p_account_id", accountId);
        inParams.put("p_card_number", pan);
        inParams.put("p_card_holder_name", holder);
        inParams.put("p_expiration_date", exp);
        inParams.put("p_cvv_hash", cvv);
        inParams.put("p_pin_hash", pin);

        Map<String, Object> result = executeFunction(SP_CREATE_CARD, inParams);
        return (UUID) result.get("returnvalue");
    }

    public Optional<UUID> findAccountIdByCardNumber(String cardNumber) {
        String sql = "SELECT account_id FROM cards WHERE card_number = ? AND status = 'ACTIVE'";
        try {
            UUID accountId = jdbcTemplate.queryForObject(sql, UUID.class, cardNumber);
            return Optional.ofNullable(accountId);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<UUID> findAccountIdByIban(String iban) {
        String sql = "SELECT id FROM accounts WHERE iban = ? AND status = 'ACTIVE'";
        try {
            UUID accountId = jdbcTemplate.queryForObject(sql, UUID.class, iban);
            return Optional.ofNullable(accountId);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public UUID processTransfer(UUID senderId, UUID receiverId, BigDecimal amount) {
        Map<String, Object> inParams = new HashMap<>();
        inParams.put("p_sender_id", senderId);
        inParams.put("p_receiver_id", receiverId);
        inParams.put("p_amount", amount);

        Map<String, Object> result = executeFunction("sp_process_transfer", inParams);
        return (UUID) result.get("returnvalue");
    }

    public void updateBalance(UUID accountId, BigDecimal newBalance) {
        String sql = "UPDATE accounts SET balance = ? WHERE id = ?";
        jdbcTemplate.update(sql, newBalance, accountId);
    }

    public Optional<Account> findById(UUID id) {
        String sql = "SELECT * FROM accounts WHERE id = ?";
        List<Account> results = queryList(sql, accountRowMapper, id);
        return results.stream().findFirst();
    }
}
