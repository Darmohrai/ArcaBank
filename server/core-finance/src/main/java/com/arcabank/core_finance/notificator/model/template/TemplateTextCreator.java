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

    public static String generateCardCreatedText(String maskedPan) {
        return String.format(
            "Ваша нова платіжна картка %s успішно випущена та готова до використання. " +
                "Ви можете переглянути її деталі та налаштувати ліміти у своєму кабінеті.",
            maskedPan
        );
    }

    public static String generateTransferIncomeText(java.math.BigDecimal amount, String currency) {
        return String.format("На ваш рахунок зараховано %s %s.", amount.toPlainString(), currency);
    }

    public static String generateTransferExpenseText(java.math.BigDecimal amount, String currency) {
        return String.format("З вашого рахунку успішно списано %s %s.", amount.toPlainString(), currency);
    }
}
