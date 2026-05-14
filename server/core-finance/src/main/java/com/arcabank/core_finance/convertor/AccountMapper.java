package com.arcabank.core_finance.convertor;

import com.arcabank.core_finance.dto.AccountDto;
import com.arcabank.core_finance.exception.AppException;
import com.arcabank.core_finance.model.Account;
import com.arcabank.core_finance.model.util.AccountStatus;
import com.arcabank.core_finance.model.util.Currency;
import com.arcabank.core_finance.utils.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AccountMapper {

    public AccountDto toDto(Account account) {
        if (account == null) {
            return null;
        }

        return AccountDto.builder()
            .id(account.getId())
            .userId(account.getUserId())
            .iban(account.getIban())
            .currency(account.getCurrency() != null ? account.getCurrency().name() : null)
            .balance(account.getBalance())
            .status(account.getStatus() != null ? account.getStatus().name() : null)
            .createdAt(account.getCreatedAt())
            .build();
    }

    public Account toEntity(AccountDto dto) {
        if (dto == null) {
            return null;
        }

        Account.AccountBuilder accountBuilder = Account.builder()
            .id(dto.getId())
            .userId(dto.getUserId())
            .iban(dto.getIban())
            .balance(dto.getBalance())
            .createdAt(dto.getCreatedAt());

        if (dto.getCurrency() != null && !dto.getCurrency().trim().isEmpty()) {
            try {
                accountBuilder.currency(Currency.valueOf(dto.getCurrency().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    "Unsupported currency: " +  dto.getCurrency()
                );
            }
        }

        if (dto.getStatus() != null && !dto.getStatus().trim().isEmpty()) {
            try {
                accountBuilder.status(AccountStatus.valueOf(dto.getStatus().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    "Unsupported status: " + dto.getStatus()
                );
            }
        }

        return accountBuilder.build();
    }

    public List<AccountDto> toDtoList(List<Account> accounts) {
        if (accounts == null) {
            return java.util.Collections.emptyList();
        }

        return accounts.stream()
            .map(this::toDto)
            .collect(java.util.stream.Collectors.toList());
    }
}
