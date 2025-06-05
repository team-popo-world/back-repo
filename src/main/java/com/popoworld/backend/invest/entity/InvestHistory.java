package com.popoworld.backend.invest.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

@Document(collection = "invest_history")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class InvestHistory {

    @Id
    private UUID id;

    @NotNull
    private UUID investSessionId; //게임 구분

    @NotNull
    private UUID chapterId; //챕터 구분

    @NotNull
    private UUID childId; //사용자 구분

    @NotNull
    private Integer turn; //턴 구분

    @NotNull
    private String riskLevel; //종목 위험도

    @NotNull
    private Integer currentPoint; //현재 포인트


    @NotNull
    private Integer beforeValue; //전 가격

    @NotNull
    private Integer currentValue; //현재 가격

    @NotNull
    private Integer initialValue; //종목의 초기값.

    private Integer numberOfShares; //수량

    private Integer income; //종목 sell 시점 시세차익

    private String transactionType; //"BUY", "SELL"

    private Integer plusClick;

    private Integer minusClick;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startedAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endedAt; //각 턴 끝나는 시간



}
