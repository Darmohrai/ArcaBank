package com.arcabank.core_finance.service;

import com.arcabank.core_finance.dto.EscrowInitiationRequest;
import com.arcabank.core_finance.dto.EscrowInitiationResponse;
import com.arcabank.core_finance.dto.PendingEscrowResponse;
import com.arcabank.core_finance.dto.VoteRequest;
import com.arcabank.core_finance.exception.AppException;
import com.arcabank.core_finance.model.Chest;
import com.arcabank.core_finance.model.ChestMember;
import com.arcabank.core_finance.model.EscrowTransaction;
import com.arcabank.core_finance.model.EscrowVote;
import com.arcabank.core_finance.model.util.ChestMemberRole;
import com.arcabank.core_finance.model.util.EscrowStatus;
import com.arcabank.core_finance.notificator.model.outbox.NotificationOutbox;
import com.arcabank.core_finance.notificator.repository.NotificationOutboxRepository;
import com.arcabank.core_finance.repository.*;
import com.arcabank.core_finance.utils.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EscrowService {
    private final ChestRepository chestRepository;
    private final ChestMemberRepository chestMemberRepository;
    private final EscrowTransactionRepository escrowTransactionRepository;
    private final NotificationOutboxRepository notificationOutboxRepository;
    private final EscrowVoteRepository escrowVoteRepository;
    private final TransactionService transactionService;

    @Transactional
    public EscrowInitiationResponse initiateWithdrawal(UUID chestId, UUID userId, EscrowInitiationRequest request) {
        ChestMember member = chestMemberRepository.findByChestIdAndUserId(chestId, userId)
            .orElseThrow(() -> new AppException(ErrorCode.NOT_CHEST_ACCESS, "Ви не є учасником цієї скрині"));

        if (escrowTransactionRepository.existsByChestIdAndStatus(chestId, EscrowStatus.PENDING.name())) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Завершіть поточне голосування");
        }

        Chest chest = chestRepository.findChestById(chestId)
            .orElseThrow(() -> new AppException(ErrorCode.CHEST_NOT_FOUND, "Скриню не знайдено"));

        if (request.amount().compareTo(chest.getBalance()) > 0) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Недостатньо доступних коштів");
        }

        chestRepository.freezeFunds(chestId, request.amount());

        EscrowTransaction escrowTx = EscrowTransaction.builder()
            .id(UUID.randomUUID())
            .chestId(chestId)
            .initiatorId(userId)
            .amount(request.amount())
            .destinationAccountId(request.destinationAccount())
            .purpose(request.purpose())
            .status(EscrowStatus.PENDING)
            .build();
        escrowTransactionRepository.save(escrowTx);

        List<ChestMember> allMembers = chestMemberRepository.findByChestId(chestId);
        List<ChestMember> voters = allMembers.stream()
            .filter(m -> !m.getUserId().equals(userId))
            .toList();

        for (ChestMember voter : voters) {
            String payload = String.format("{\"eventType\": \"ESCROW_VOTE_REQUIRED\", \"escrowId\": \"%s\", \"trusteeId\": \"%s\"}",
                escrowTx.getId(), voter.getUserId());

            NotificationOutbox outboxMessage = NotificationOutbox.builder()
                .topic("notification-events")
                .payload(payload)
                .status(NotificationOutbox.OutboxStatus.PENDING)
                .retryCount(0)
                .build();

            notificationOutboxRepository.insert(outboxMessage);
        }

        return new EscrowInitiationResponse(
            "Запит на виведення успішно створено",
            escrowTx.getId(),
            chestId,
            escrowTx.getAmount(),
            EscrowStatus.PENDING.name()
        );
    }

    @Transactional
    public void processVote(UUID escrowId, UUID userId, VoteRequest request) {
        EscrowTransaction escrowTx = escrowTransactionRepository.findByIdForUpdate(escrowId)
            .orElseThrow(() -> new AppException(ErrorCode.TRANSACTION_NOT_FOUND, "Транзакцію не знайдено"));

        if (escrowTx.getStatus() != EscrowStatus.PENDING) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Голосування по цьому запиту вже закрито");
        }

        chestMemberRepository.findByChestIdAndUserId(escrowTx.getChestId(), userId)
            .orElseThrow(() -> new AppException(ErrorCode.NOT_CHEST_ACCESS, "Ви не є учасником скрині"));

        if (escrowTx.getInitiatorId().equals(userId)) {
            throw new AppException(ErrorCode.NOT_CHEST_ACCESS, "Ініціатор запиту не може за нього голосувати");
        }

        if (escrowVoteRepository.existsByEscrowTransactionIdAndUserId(escrowId, userId)) {
            throw new AppException(ErrorCode.ALREADY_VOTE, "Ви вже проголосували за цей запит");
        }

        if (!request.decision()) {
            escrowTransactionRepository.updateStatus(escrowId, EscrowStatus.REJECTED.name());
            chestRepository.unfreezeFunds(escrowTx.getChestId(), escrowTx.getAmount());
            log.info("Escrow {} was REJECTED by user {}", escrowId, userId);
            return;
        }

        EscrowVote vote = EscrowVote.builder()
            .id(UUID.randomUUID())
            .escrowTransactionId(escrowId)
            .userId(userId)
            .decision("APPROVED")
            .createdAt(LocalDateTime.now())
            .build();

        escrowVoteRepository.save(vote);

        int totalMembers = chestMemberRepository.findByChestId(escrowTx.getChestId()).size();
        int requiredApprovals = totalMembers - 1;
        int totalApprovals = escrowVoteRepository.countApprovals(escrowId);

        if (totalApprovals >= requiredApprovals) {
            escrowTransactionRepository.updateStatus(escrowId, EscrowStatus.APPROVED.name());
            chestRepository.finalizeEscrowFunds(escrowTx.getChestId(), escrowTx.getAmount());

            Chest chest = chestRepository.findChestById(escrowTx.getChestId())
                .orElseThrow(() -> new AppException(ErrorCode.CHEST_NOT_FOUND, "Скриню не знайдено"));

            transactionService.processEscrowTransfer(
                chest.getAccountId(),
                escrowTx.getDestinationAccountId(),
                escrowTx.getAmount()
            );
        }
    }

    @Transactional(readOnly = true)
    public PendingEscrowResponse getPendingEscrow(UUID chestId, UUID userId) {
        chestMemberRepository.findByChestIdAndUserId(chestId, userId)
            .orElseThrow(() -> new AppException(ErrorCode.NOT_CHEST_ACCESS, "Ви не є учасником цієї скрині"));

        EscrowTransaction escrowTx = escrowTransactionRepository.findPendingByChestId(chestId)
            .orElseThrow(() -> new AppException(ErrorCode.TRANSACTION_NOT_FOUND, "Немає активних голосувань для цієї скрині"));

        int totalMembers = chestMemberRepository.findByChestId(chestId).size();
        int requiredApprovals = totalMembers - 1;
        int approvalsCount = escrowVoteRepository.countApprovals(escrowTx.getId());

        boolean currentUserVoted = escrowVoteRepository.existsByEscrowTransactionIdAndUserId(escrowTx.getId(), userId);
        boolean isInitiator = escrowTx.getInitiatorId().equals(userId);

        boolean canCurrentUserVote = !isInitiator && !currentUserVoted;

        return new PendingEscrowResponse(
            escrowTx.getId(),
            escrowTx.getChestId(),
            escrowTx.getInitiatorId(),
            escrowTx.getAmount(),
            escrowTx.getDestinationAccountId(),
            escrowTx.getPurpose(),
            escrowTx.getStatus().name(),
            escrowTx.getCreatedAt(),
            approvalsCount,
            requiredApprovals,
            currentUserVoted,
            canCurrentUserVote
        );
    }
}
