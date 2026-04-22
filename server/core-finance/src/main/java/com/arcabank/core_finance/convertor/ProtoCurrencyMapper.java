package com.arcabank.core_finance.convertor;

import com.arcabank.core_finance.model.util.Currency;
import com.arcabank.grpc.ProtoCurrency;

public class ProtoCurrencyMapper {

    private ProtoCurrencyMapper() {
    }

    public static Currency mapCurrency(ProtoCurrency protoCurrency) {
        return switch (protoCurrency) {
            case CURRENCY_UAH -> Currency.UAH;
            case CURRENCY_USD -> Currency.USD;
            case CURRENCY_EUR -> Currency.EUR;
            default -> Currency.UAH;
        };
    }
}
