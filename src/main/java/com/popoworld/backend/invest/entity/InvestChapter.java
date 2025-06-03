package com.popoworld.backend.invest.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name="invest_chapter")
public class InvestChapter {

    @Id
    private UUID chapterId;

    private String chapterName;

    private Integer seedMoney;

    private String introMessages;

    @OneToMany(mappedBy = "investChapter", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    private List<InvestScenario> investScenarios;
    // fetch = FetchType.LAZY
    // InvestChapter 조회할 때 InvestSenario는 나중에 필요할 때만 로딩

    //연관관계의 주인은 FK를 실제로 가지고 있는 테이블.
    //mappedBy를 씀으로써, 연관관계의 주인이 아니란걸 나타냄.
}
