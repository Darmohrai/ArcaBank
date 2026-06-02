package com.arcabank.core_finance.service;

import com.arcabank.core_finance.dto.EscrowInitiationRequest;
import com.arcabank.core_finance.exception.AppException;
import com.arcabank.core_finance.model.Chest;
import com.arcabank.core_finance.model.ChestMember;
import com.arcabank.core_finance.model.EscrowTransaction;
import com.arcabank.core_finance.model.util.ChestMemberRole;
import com.arcabank.core_finance.model.util.EscrowStatus;
import com.arcabank.core_finance.notificator.model.outbox.NotificationOutbox;
import com.arcabank.core_finance.notificator.repository.NotificationOutboxRepository;
import com.arcabank.core_finance.repository.ChestMemberRepository;
import com.arcabank.core_finance.repository.ChestRepository;
import com.arcabank.core_finance.repository.EscrowTransactionRepository;
import com.arcabank.core_finance.utils.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public void initiateWithdrawal(UUID chestId, UUID userId, EscrowInitiationRequest request) {
        ChestMember member = chestMemberRepository.findByChestIdAndUserId(chestId, userId)
            .orElseThrow(() -> new AppException(ErrorCode.NOT_CHEST_ACCESS, "Ви не є учасником цієї скрині"));

        if (member.getRole() != ChestMemberRole.OWNER) {
            throw new AppException(ErrorCode.NOT_CHEST_ACCESS, "Тільки власник може ініціювати виведення коштів");
        }

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

        List<ChestMember> trustees = chestMemberRepository.findByChestIdAndRole(chestId, ChestMemberRole.TRUSTEE);

        for (ChestMember trustee : trustees) {
            String payload = String.format("{\"eventType\": \"ESCROW_VOTE_REQUIRED\", \"escrowId\": \"%s\", \"trusteeId\": \"%s\"}",
                escrowTx.getId(), trustee.getUserId());

            NotificationOutbox outboxMessage = NotificationOutbox.builder()
                .topic("notification-events")
                .payload(payload)
                .status(NotificationOutbox.OutboxStatus.PENDING)
                .retryCount(0)
                .build();

            notificationOutboxRepository.insert(outboxMessage);
        }
    }
}
