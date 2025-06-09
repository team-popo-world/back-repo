package com.popoworld.backend.quest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParentQuestRequest {
    private UUID childId;
    private UUID parentId;
    private String name;
    private String description;
    private Integer reward;
    private String endDate;
    private String imageUrl;
}
