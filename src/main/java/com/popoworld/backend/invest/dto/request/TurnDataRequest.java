package com.popoworld.backend.invest.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TurnDataRequest {
    private String sessionId;

    @JsonProperty("started_at")
    private String startedAt;

    @JsonProperty("ended_at")
    private String endedAt;

    @JsonProperty("risk_level")
    private String riskLevel;

    @JsonProperty("current_point")
    private Integer currentPoint;

    @JsonProperty("before_value")
    private Integer beforeValue;

    @JsonProperty("current_value")
    private Integer currentValue;

    @JsonProperty("initial_value")
    private Integer initialValue;

    @JsonProperty("number_of_shares")
    private Integer numberOfShares;

    private Integer income;

    @JsonProperty("transaction_type")
    private String transactionType;

    @JsonProperty("plus_click")
    private Integer plusClick;

    @JsonProperty("minus_click")
    private Integer minusClick;
}