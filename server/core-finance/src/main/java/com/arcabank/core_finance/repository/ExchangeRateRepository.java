package com.arcabank.core_finance.repository;

import com.arcabank.core_finance.model.ExchangeRate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ExchangeRateRepository extends BaseRepository<ExchangeRate> {

    public ExchangeRateRepository(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    private final RowMapper<ExchangeRate> rateRowMapper = (rs, rowNum) -> ExchangeRate.builder()
        .id(UUID.fromString(rs.getString("id")))
        .currency(rs.getString("currency"))
        .baseCurrency(rs.getString("base_currency"))
        .buyRate(rs.getBigDecimal("buy_rate"))
        .sellRate(rs.getBigDecimal("sell_rate"))
        .updatedAt(rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null)
        .build();

    public void upsertRate(String currency, BigDecimal buyRate, BigDecimal sellRate) {
        String sql = """
            INSERT INTO exchange_rates (currency, base_currency, buy_rate, sell_rate, updated_at)
            VALUES (?, 'UAH', ?, ?, CURRENT_TIMESTAMP)
            ON CONFLICT (currency)
            DO UPDATE SET
                buy_rate = EXCLUDED.buy_rate,
                sell_rate = EXCLUDED.sell_rate,
                updated_at = CURRENT_TIMESTAMP;
            """;
        update(sql, currency, buyRate, sellRate);
    }

    public Optional<ExchangeRate> findByCurrency(String currency) {
        String sql = "SELECT * FROM exchange_rates WHERE currency = ?";
        return queryList(sql, rateRowMapper, currency).stream().findFirst();
    }

    public List<ExchangeRate> findAll() {
        String sql = "SELECT * FROM exchange_rates ORDER BY currency ASC";
        return queryList(sql, rateRowMapper);
    }
}
