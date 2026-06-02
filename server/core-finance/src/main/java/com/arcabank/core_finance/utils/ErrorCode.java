package com.arcabank.core_finance.utils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    VALIDATION_ERROR("Помилка валідації вхідних даних", HttpStatus.BAD_REQUEST),
    INSUFFICIENT_FUNDS("Недостатньо коштів на рахунку", HttpStatus.BAD_REQUEST),
    INVALID_DESTINATION("Невірний формат реквізитів отримувача", HttpStatus.BAD_REQUEST),
    SAME_CURRENCY("Валюти обміну мають бути різними", HttpStatus.BAD_REQUEST),
    ALREADY_BLOCKED("Ресурс вже заблоковано", HttpStatus.BAD_REQUEST),
    ALREADY_ACTIVE("Ресурс і так активний", HttpStatus.BAD_REQUEST),
    LIMIT_EXCEEDED("Перевищено ліміт операції", HttpStatus.BAD_REQUEST),
    RESTRICTED_ROUTE("Цей напрямок переказу заборонено", HttpStatus.BAD_REQUEST),
    CHEST_CLOSED("Рахунок закритий", HttpStatus.BAD_REQUEST),
    NOT_CHEST_ACCESS("Вам рахунок не доступний", HttpStatus.FORBIDDEN),

    ACCOUNT_NOT_FOUND("Рахунок не знайдено", HttpStatus.NOT_FOUND),
    CARD_NOT_FOUND("Картку не знайдено", HttpStatus.NOT_FOUND),
    USER_NOT_FOUND("Користувача не знайдено", HttpStatus.NOT_FOUND),
    CHEST_NOT_FOUND("Скриню не знайдено", HttpStatus.NOT_FOUND),

    ACCESS_DENIED("Доступ заборонено. Це не ваш ресурс.", HttpStatus.FORBIDDEN),


    INTERNAL_ERROR("Внутрішня помилка сервера", HttpStatus.INTERNAL_SERVER_ERROR),
    TRANSFER_ERROR("Помилка виконання переказу", HttpStatus.INTERNAL_SERVER_ERROR),
    CARD_GENERATION_FAILED("Не вдалося згенерувати унікальну картку", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String defaultMessage;
    private final HttpStatus status;
}
