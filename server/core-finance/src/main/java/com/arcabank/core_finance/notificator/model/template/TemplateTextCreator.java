package com.arcabank.core_finance.notificator.model.template;

import com.arcabank.core_finance.model.util.Currency;

public class TemplateTextCreator {

    private TemplateTextCreator() {
    }

    public static String generateAccountCreatedText(Currency currency) {
        return String.format(
                "Вітаємо! Ваш новий рахунок у валюті %s успішно активовано в ArcaBank. " +
                        "Тепер ви можете вільно отримувати перекази, зберігати заощадження " +
                        "та керувати своїми коштами.",
                currency != null ? currency : "UAH"
        );

    }

    public static String generateAccountBlockedText(String iban) {
        return "";
    }

    public static String generateAccountUnblockedText(String iban) {
        return "";
    }

}
