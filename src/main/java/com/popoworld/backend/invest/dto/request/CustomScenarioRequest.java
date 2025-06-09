package com.popoworld.backend.invest.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CustomScenarioRequest {
    private String chapterId;
    private String story;
    private Boolean isCustom;
}
