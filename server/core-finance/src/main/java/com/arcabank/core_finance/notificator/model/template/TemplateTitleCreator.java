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

    public static String generateCardCreatedTitle() {
        return "💳 Нову картку випущено!";
    }

    public static String generateTransferIncomeTitle() {
        return "💸 Надходження коштів!";
    }

    public static String generateTransferExpenseTitle() {
        return "📤 Переказ надіслано";
    }

    public static String generateCardBlockedTitle() {
        return "🔒 Картку заблоковано";
    }

    public static String generateCardUnblockedTitle() {
        return "🔓 Картку розблоковано";
    }

    public static String generateNotEnoughMoneyTitle() {
        return "❌ Відмова: Недостатньо коштів";
    }

    public static String generatePaymentFailedTitle() {
        return "⚠️ Помилка переказу";
    }
}
