package com.arcabank.core_finance.service;

import com.arcabank.core_finance.convertor.AccountMapper;
import com.arcabank.core_finance.dto.AccountDto;
import com.arcabank.core_finance.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;

    public List<AccountDto> getAccountsByUserId(UUID userId) {
        return accountMapper.toDtoList(
            accountRepository.findAllByUserId(userId)
        );
    }
}
