package com.popoworld.backend.quest.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestResponse {
    @JsonProperty("quest_id")
    private UUID questId;

    @JsonProperty("child_id")
    private UUID childId;

    private String type;

    private String name;

    private String description;

    private String state;

    @JsonProperty("end_date")
    private LocalDateTime endDate;

    private LocalDateTime created;

    @JsonProperty("isStatic")
    private Boolean isStatic;

    private Integer reward;

    @JsonProperty("imageUrl")
    private String imageUrl;
}
