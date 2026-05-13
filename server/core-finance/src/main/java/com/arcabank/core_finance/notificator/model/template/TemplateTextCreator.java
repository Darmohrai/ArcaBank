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

    public static String generateCardBlockedText(String maskedPan) {
        return String.format(
                "Вашу картку %s було тимчасово заблоковано. Операції з нею наразі недоступні.",
                maskedPan
        );
    }

    public static String generateCardUnblockedText(String maskedPan) {
        return String.format(
                "Вашу картку %s успішно розблоковано. Ви знову можете здійснювати операції.",
                maskedPan
        );
    }

    public static String generateNotEnoughMoneyText(java.math.BigDecimal amount, String currency) {
        return String.format(
                "Спроба переказу на суму %s %s відхилена. На вашому рахунку недостатньо коштів для здійснення цієї операції.",
                amount.toPlainString(), currency
        );
    }

    public static String generatePaymentFailedText(java.math.BigDecimal amount, String currency, String reason) {
        return String.format(
                "Ваш переказ на суму %s %s не було виконано. Причина: %s. Будь ласка, перевірте реквізити або спробуйте пізніше.",
                amount.toPlainString(), currency, reason
        );
    }
}
