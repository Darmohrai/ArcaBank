package com.arcabank.core_finance.service;

import com.arcabank.core_finance.client.UserClient;
import com.arcabank.core_finance.dto.ChestCreationRequest;
import com.arcabank.core_finance.dto.ChestResponse;
import com.arcabank.core_finance.exception.AppException;
import com.arcabank.core_finance.model.Chest;
import com.arcabank.core_finance.model.util.ChestMemberRole;
import com.arcabank.core_finance.model.util.ChestStatus;
import com.arcabank.core_finance.repository.ChestMemberRepository;
import com.arcabank.core_finance.repository.ChestRepository;
import com.arcabank.core_finance.utils.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final UserClient userClient;

    @Transactional
    public ChestResponse createChest(UUID creatorId, ChestCreationRequest request) {
        Chest chest = Chest.builder()
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
                } catch (Exception e) {
                    throw new AppException(ErrorCode.USER_NOT_FOUND, "User not found");
                }

                chestMemberRepository.addChestMember(chestId, friendId, ChestMemberRole.TRUSTEE, LocalDateTime.now());
            }
        }

        return new ChestResponse(
            chestId,
            request.name(),
            request.targetAmount(),
            ChestStatus.ACTIVE.toString()
        );
    }
}
