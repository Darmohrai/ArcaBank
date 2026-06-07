package com.arcabank.core_finance.model;

import com.arcabank.core_finance.model.util.ChestMemberRole;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChestMember {
    private UUID chestId;
    private UUID userId;
    private ChestMemberRole role;
    private LocalDateTime joinedAt;
}
