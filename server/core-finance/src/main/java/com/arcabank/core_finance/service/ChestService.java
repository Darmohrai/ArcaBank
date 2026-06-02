package com.arcabank.core_finance.service;

import com.arcabank.core_finance.client.UserClient;
import com.arcabank.core_finance.dto.*;
import com.arcabank.core_finance.exception.AppException;
import com.arcabank.core_finance.model.Account;
import com.arcabank.core_finance.model.Chest;
import com.arcabank.core_finance.model.ChestMember;
import com.arcabank.core_finance.model.util.AccountType;
import com.arcabank.core_finance.model.util.ChestMemberRole;
import com.arcabank.core_finance.model.util.ChestStatus;
import com.arcabank.core_finance.repository.AccountRepository;
import com.arcabank.core_finance.repository.ChestMemberRepository;
import com.arcabank.core_finance.repository.ChestRepository;
import com.arcabank.core_finance.utils.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChestService {
    private final ChestRepository chestRepository;
    private final ChestMemberRepository chestMemberRepository;
    private final AccountRepository accountRepository;
    private final AccountService accountService;
    private final UserClient userClient;

    @Transactional
    public ChestResponse createChest(UUID creatorId, ChestCreationRequest request) {

        String currency = request.currency() != null && !request.currency().isEmpty() ? request.currency() : "UAH";
        String pin = request.pin() != null ? request.pin() : "0000";

        AccountCreationRequest accountCreationRequest = new AccountCreationRequest(
            currency,
            AccountType.CHEST,
            pin
        );

        AccountResponse accountResponse = accountService.createAccountWithCard(creatorId, accountCreationRequest);

        Chest chest = Chest.builder()
            .accountId(accountResponse.accountId())
            .name(request.name())
            .targetAmount(request.targetAmount())
            .description(request.description())
            .status(ChestStatus.ACTIVE)
            .build();

        UUID chestId = chestRepository.createChest(chest);

        chestMemberRepository.addChestMember(chestId, creatorId, ChestMemberRole.OWNER, LocalDateTime.now());

        if (request.friends() != null && !request.friends().isEmpty()) {
            Set<UUID> uniqueFriends = new HashSet<>(request.friends());
            uniqueFriends.remove(creatorId);

            for (UUID friendId : uniqueFriends) {
                try {
                    userClient.getUserById(friendId);
                    chestMemberRepository.addChestMember(chestId, friendId, ChestMemberRole.TRUSTEE, LocalDateTime.now());
                } catch (Exception e) {
                    throw new AppException(ErrorCode.USER_NOT_FOUND, "User not found");
                }
            }
        }

        return new ChestResponse(
            chestId,
            request.name(),
            request.targetAmount(),
            ChestStatus.ACTIVE.toString(),
            accountResponse.iban(),
            accountResponse.cardNumber()
        );
    }

    @Transactional
    public ChestDepositResponse depositToChest(UUID userId, UUID chestId, ChestDepositRequest request) {
        Chest chest = chestRepository.findChestById(chestId)
            .orElseThrow(() -> new AppException(ErrorCode.CHEST_NOT_FOUND, "Chest not found"));

        if (chest.getStatus() != ChestStatus.ACTIVE) {
            throw new AppException(ErrorCode.CHEST_CLOSED, "Chest is not active");
        }

        ChestMember member = chestMemberRepository.findByChestIdAndUserId(chestId, userId)
            .orElseThrow(() -> new AppException(ErrorCode.ACCESS_DENIED, "Дія доступна лише для учасників скрині"));

        if (member.getRole() != ChestMemberRole.OWNER && member.getRole() != ChestMemberRole.TRUSTEE) {
            throw new AppException(ErrorCode.ACCESS_DENIED, "Дія доступна лише для учасників скрині з роллю OWNER або TRUSTEE");
        }

        Account userAccount = accountRepository.findById(request.senderAccountId())
            .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND, "Особистий рахунок користувача не знайдено"));

        if (!userAccount.getUserId().equals(userId)) {
            throw new AppException(ErrorCode.ACCESS_DENIED, "Цей рахунок не ваш");
        }

        if (userAccount.getBalance().compareTo(request.amount()) < 0) {
            throw new AppException(ErrorCode.INSUFFICIENT_FUNDS, "Недостатньо коштів на рахунку для переказу");
        }

        chestRepository.processChestDeposit(request.senderAccountId(), chestId, request.amount());

        log.info("Deposit of {} to chest {} completed successfully", request.amount(), chestId);

        BigDecimal newBalance = chest.getBalance().add(request.amount());

        return new ChestDepositResponse(
            "Скриню успішно поповнено",
            chestId,
            newBalance
        );
    }
}
