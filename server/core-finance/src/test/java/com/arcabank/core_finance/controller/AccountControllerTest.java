package com.arcabank.core_finance.controller;

import com.arcabank.core_finance.client.UserClient;
import com.arcabank.core_finance.dto.AccountCreationRequest;
import com.arcabank.core_finance.dto.UserResponse;
import com.arcabank.core_finance.model.util.AccountType;
import com.arcabank.core_finance.service.AccountService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc; // Інструмент для імітації HTTP-запитів

    @Autowired
    private ObjectMapper objectMapper; // Для перетворення об'єктів у JSON

    @MockBean
    private AccountService accountService; // Створюємо "заглушку" для сервісу

    @MockBean
    private UserClient userClient; // Створюємо "заглушку" для Feign Client

    @Test
    void createAccount_ShouldPassValidationAndPrintLogs() throws Exception {
        // 1. ПІДГОТОВКА ДАНИХ (Arrange)
        UUID mockUserId = UUID.randomUUID();
        AccountCreationRequest request = new AccountCreationRequest("UAH", AccountType.SAVINGS, "0000");

        // Навчаємо нашого "фейкового" клієнта:
        // "Коли тебе попросять дані за цим ID, поверни Тараса Шевченка"
        when(userClient.getUserById(mockUserId))
            .thenReturn(new UserResponse("Тарас", "Шевченко"));

        // 2. ДІЯ (Act) ТА ПЕРЕВІРКА (Assert)
        mockMvc.perform(post("/api/v1/accounts")
                // Ось тут магія: генеруємо фейковий токен прямо в пам'яті!
                .with(jwt().jwt(jwt -> jwt.subject(mockUserId.toString())))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            // Очікуємо, що статус буде 200 OK (поки що)
            .andExpect(status().isOk());
    }

    @Test
    void createAccount_ShouldReturn400_WhenCurrencyIsInvalid() throws Exception {
        AccountCreationRequest request = new AccountCreationRequest("PLN", AccountType.SAVINGS, "0000");

        mockMvc.perform(post("/api/v1/accounts")
                // Задаємо валідний UUID, щоб тест не падав через нього
                .with(jwt().jwt(jwt -> jwt.subject(UUID.randomUUID().toString())))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }
}
