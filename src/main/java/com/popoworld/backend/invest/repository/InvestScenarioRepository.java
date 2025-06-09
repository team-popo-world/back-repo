package com.popoworld.backend.invest.repository;

import com.popoworld.backend.invest.entity.InvestScenario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface InvestScenarioRepository extends JpaRepository<InvestScenario, UUID> {
    InvestScenario findByInvestChapter_ChapterId(String chapterId);
    //실제 실행되는 쿼리
    //SELECT * FROM invest_scenario
    //WHERE chapter_id = ?
    //InvestScenario 엔티티의 investChapter 필드

    InvestScenario findTopByUpdatedAtIsNullOrderByCreateAtAsc();

    // 새로 추가 - 특정 챕터 중에서 가장 오래된 미업데이트 시나리오 찾기
    InvestScenario findTopByInvestChapter_ChapterIdAndUpdatedAtIsNullOrderByCreateAtAsc(String chapterId);
}
