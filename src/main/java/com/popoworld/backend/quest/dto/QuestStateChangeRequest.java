package com.popoworld.backend.quest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestStateChangeRequest {
    private UUID questId;

    private UUID childId;

    private String state;
}
