package com.arcabank.core_finance.service;

import com.arcabank.core_finance.convertor.AccountMapper;
import com.arcabank.core_finance.dto.AccountDto;
import com.arcabank.core_finance.dto.AccountOnlyRequest;
import com.arcabank.core_finance.exception.AppException;
import com.arcabank.core_finance.model.Account;
import com.arcabank.core_finance.notificator.engine.Notificator;
import com.arcabank.core_finance.repository.AccountRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static com.arcabank.core_finance.model.util.AccountType.CHECKING;
import static com.arcabank.core_finance.model.util.AccountType.SAVINGS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccountService Tests")
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountMapper accountMapper;

    @Mock
    private Notificator notificator;

    @InjectMocks
    private AccountService accountService;

    @Nested
    @DisplayName("getAccountsByUserId() Tests")
    class GetAccountsByUserIdTests {

        @Test
        @DisplayName("Should return list of AccountDto when accounts exist for user")
        void shouldReturnListOfAccountDtoWhenAccountsExist() {
            UUID userId = UUID.randomUUID();
            Account account1 = new Account();
            Account account2 = new Account();
            List<Account> accounts = List.of(account1, account2);

            AccountDto accountDto1 = new AccountDto();
            AccountDto accountDto2 = new AccountDto();
            List<AccountDto> expectedDtos = List.of(accountDto1, accountDto2);

            when(accountRepository.findAllByUserId(userId)).thenReturn(accounts);
            when(accountMapper.toDtoList(accounts)).thenReturn(expectedDtos);

            List<AccountDto> actualDtos = accountService.getAccountsByUserId(userId);

            assertThat(actualDtos)
                .isNotNull()
                .hasSize(2)
                .containsExactlyElementsOf(expectedDtos);

            verify(accountRepository).findAllByUserId(userId);
            verify(accountMapper).toDtoList(accounts);
        }

        @Test
        @DisplayName("Should return empty list when no accounts exist for user")
        void shouldReturnEmptyListWhenNoAccountsExist() {
            UUID userId = UUID.randomUUID();
            List<Account> emptyAccountsList = Collections.emptyList();
            List<AccountDto> emptyDtosList = Collections.emptyList();

            when(accountRepository.findAllByUserId(userId)).thenReturn(emptyAccountsList);
            when(accountMapper.toDtoList(emptyAccountsList)).thenReturn(emptyDtosList);

            List<AccountDto> actualDtos = accountService.getAccountsByUserId(userId);

            assertThat(actualDtos)
                .isNotNull()
                .isEmpty();

            verify(accountRepository).findAllByUserId(userId);
            verify(accountMapper).toDtoList(emptyAccountsList);
        }

        @Test
        @DisplayName("Should handle null userId properly")
        void shouldHandleNullUserId() {
            List<Account> emptyAccountsList = Collections.emptyList();
            List<AccountDto> emptyDtosList = Collections.emptyList();

            when(accountRepository.findAllByUserId(null)).thenReturn(emptyAccountsList);
            when(accountMapper.toDtoList(emptyAccountsList)).thenReturn(emptyDtosList);

            List<AccountDto> actualDtos = accountService.getAccountsByUserId(null);

            assertThat(actualDtos)
                .isNotNull()
                .isEmpty();

            verify(accountRepository).findAllByUserId(null);
            verify(accountMapper).toDtoList(emptyAccountsList);
        }

        @Test
        @DisplayName("Should propagate exception when repository throws RuntimeException")
        void shouldPropagateExceptionFromRepository() {
            UUID userId = UUID.randomUUID();
            RuntimeException dbException = new RuntimeException("Database connection error");

            when(accountRepository.findAllByUserId(userId)).thenThrow(dbException);

            assertThatThrownBy(() -> accountService.getAccountsByUserId(userId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Database connection error");

            verify(accountRepository).findAllByUserId(userId);
            verifyNoInteractions(accountMapper);
        }

        @Test
        @DisplayName("Should propagate exception when mapper throws RuntimeException")
        void shouldPropagateExceptionFromMapper() {
            UUID userId = UUID.randomUUID();
            Account account = new Account();
            List<Account> accounts = List.of(account);
            RuntimeException mappingException = new RuntimeException("Mapping error");

            when(accountRepository.findAllByUserId(userId)).thenReturn(accounts);
            when(accountMapper.toDtoList(accounts)).thenThrow(mappingException);

            assertThatThrownBy(() -> accountService.getAccountsByUserId(userId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Mapping error");

            verify(accountRepository).findAllByUserId(userId);
            verify(accountMapper).toDtoList(accounts);
        }

        @Test
        @DisplayName("openNewAccount should retry and succeed when DuplicateKeyException thrown onc")
        void openNewAccount_shouldRetryAndSucceed_whenDuplicateKeyExceptionThrownOnce() {
            UUID userId = UUID.randomUUID();
            AccountOnlyRequest request = new AccountOnlyRequest("UAH", SAVINGS);
            UUID expectedAccountId = UUID.randomUUID();
            AccountDto expectedDto = AccountDto.builder().id(expectedAccountId).build();

            when(accountRepository.createJustAccount(any(Account.class)))
                .thenThrow(new DuplicateKeyException("Duplicate IBAN"))
                .thenReturn(expectedAccountId);

            when(accountMapper.toDto(any(Account.class))).thenReturn(expectedDto);

            AccountDto result = accountService.openNewAccount(userId, request);

            assertNotNull(result);
            assertEquals(expectedAccountId, result.getId());

            verify(accountRepository, times(2)).createJustAccount(any(Account.class));

            verify(notificator, times(1)).notifyAccountCreated(any(Account.class));
        }

        @Test
        @DisplayName("openNewAccount should throw AppException when MaxRetries exceeded")
        void openNewAccount_shouldThrowAppException_whenMaxRetriesExceeded() {
            UUID userId = UUID.randomUUID();
            AccountOnlyRequest request = new AccountOnlyRequest("USD", CHECKING);

            when(accountRepository.createJustAccount(any(Account.class)))
                .thenThrow(new DuplicateKeyException("Duplicate IBAN permanently"));

            AppException exception = assertThrows(AppException.class, () ->
                accountService.openNewAccount(userId, request)
            );

            assertEquals("Failed to generate unique IBAN", exception.getMessage());

            verify(accountRepository, times(3)).createJustAccount(any(Account.class));

            verify(notificator, never()).notifyAccountCreated(any(Account.class));
        }
    }
}
