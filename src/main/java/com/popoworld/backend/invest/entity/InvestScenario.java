package com.popoworld.backend.invest.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name="invest_scenario")
public class InvestScenario {

    @Id
    private UUID scenarioId;

    private UUID childId;

//    private UUID chapterId;

    @Column(columnDefinition = "TEXT")
    private String story;

    private Boolean isCustom; //부모가 만든건지

    @Column(name = "create_at")  // DB 컬럼명과 맞춤
    private LocalDateTime createAt;

    @Column(name = "updated_at") // DB 컬럼명과 맞춤
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id")
    private InvestChapter investChapter;

    @OneToMany(mappedBy = "investScenario", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    private List<InvestSession> investSessions;
}
