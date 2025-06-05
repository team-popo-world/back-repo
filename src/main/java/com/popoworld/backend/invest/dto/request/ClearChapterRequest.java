package com.popoworld.backend.invest.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ClearChapterRequest {
    private String sessionId;
    private Boolean success;
    private Integer profit;
}
