package com.arcabank.core_finance.notificator.model.template;

public class TemplateTitleCreator {

    private TemplateTitleCreator() {
    }

    public static String generateAccountCreatedTitle() {
        return "🎉 Рахунок успішно відкрито!";
    }

    public static String generateAccountBlockedTitle() {
        return "";
    }

    public static String generateAccountUnblockedTitle() {
        return "";
    }
}
