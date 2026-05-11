package com.arcabank.core_finance.notificator.model.template;

import com.arcabank.core_finance.model.util.Currency;

public class TemplateTextCreator {

    private TemplateTextCreator() {
    }

    public static String generateCardCreatedText(Currency currency) {
        return String.format(
            "Вітаємо! Ваш новий рахунок у валюті %s успішно активовано в ArcaBank. " +
                "Тепер ви можете вільно отримувати перекази, зберігати заощадження " +
                "та керувати своїми коштами.",
            currency != null ? currency : "UAH"
        );

    }
}
