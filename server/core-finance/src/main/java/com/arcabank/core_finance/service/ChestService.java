package com.arcabank.core_finance.service;

import com.arcabank.core_finance.client.UserClient;
import com.arcabank.core_finance.dto.*;
import com.arcabank.core_finance.exception.AppException;
import com.arcabank.core_finance.model.Account;
import com.arcabank.core_finance.model.Chest;
import com.arcabank.core_finance.model.ChestMember;
import com.arcabank.core_finance.model.EscrowTransaction;
import com.arcabank.core_finance.model.util.AccountType;
import com.arcabank.core_finance.model.util.ChestMemberRole;
import com.arcabank.core_finance.model.util.ChestStatus;
import com.arcabank.core_finance.model.util.EscrowStatus;
import com.arcabank.core_finance.repository.*;
import com.arcabank.core_finance.utils.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
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
    private final EscrowTransactionRepository escrowTransactionRepository;
    private final EscrowVoteRepository escrowVoteRepository;

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

        if (request.friendPhones() != null && !request.friendPhones().isEmpty()) {
            Set<String> uniquePhones = new HashSet<>(request.friendPhones());

            for (String phone : uniquePhones) {
                try {
                    UserPhoneResponse friend = userClient.getUserByPhone(phone);

                    if (!friend.id().equals(creatorId)) {
                        chestMemberRepository.addChestMember(chestId, friend.id(), ChestMemberRole.TRUSTEE, LocalDateTime.now());
                    }
                } catch (Exception e) {
                    throw new AppException(ErrorCode.USER_NOT_FOUND, "Користувача з номером " + phone + " не знайдено");
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

    @Transactional(readOnly = true)
    public List<Chest> getMyChests(UUID userId) {
        return chestRepository.findAllByUserId(userId);
    }

    @Transactional(readOnly = true)
    public ChestDetailResponse getChestDetails(UUID chestId, UUID userId) {
        Chest chest = chestRepository.findChestById(chestId)
            .orElseThrow(() -> new AppException(ErrorCode.CHEST_NOT_FOUND, "Скриню не знайдено"));

        chestMemberRepository.findByChestIdAndUserId(chestId, userId)
            .orElseThrow(() -> new AppException(ErrorCode.ACCESS_DENIED, "Ви не є учасником цієї скрині"));

        List<ChestMemberDto> members = chestMemberRepository.findByChestId(chestId).stream()
            .map(m -> new ChestMemberDto(m.getUserId(), m.getRole().name(), m.getJoinedAt()))
            .toList();

        List<EscrowTransaction> escrows = escrowTransactionRepository.findAllByChestId(chestId);

        int totalMembers = members.size();
        int requiredApprovals = totalMembers - 1;

        List<PendingEscrowResponse> escrowsData = escrows.stream().map(e -> {
            int approvalsCount = escrowVoteRepository.countApprovals(e.getId());
            boolean voted = escrowVoteRepository.existsByEscrowTransactionIdAndUserId(e.getId(), userId);

            boolean isInitiator = e.getInitiatorId().equals(userId);
            boolean canVote = !isInitiator && !voted && e.getStatus() == EscrowStatus.PENDING;

            return new PendingEscrowResponse(
                e.getId(), e.getChestId(), e.getInitiatorId(), e.getAmount(), e.getDestinationAccountId(),
                e.getPurpose(), e.getStatus().name(), e.getCreatedAt(), approvalsCount, requiredApprovals, voted, canVote
            );
        }).toList();

        return new ChestDetailResponse(
            chest.getId(), chest.getName(), chest.getTargetAmount(), chest.getBalance(),
            chest.getCurrency() != null ? chest.getCurrency().name() : "UAH",
            chest.getStatus().name(), members, escrowsData
        );
    }
}
