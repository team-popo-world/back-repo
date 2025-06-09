package com.popoworld.backend.invest.service;

import com.popoworld.backend.invest.dto.request.CustomScenarioRequest;
import com.popoworld.backend.invest.dto.request.DefaultScenarioRequest;
import com.popoworld.backend.invest.entity.InvestScenario;
import com.popoworld.backend.invest.repository.InvestScenarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ScenarioService {

    private final InvestScenarioRepository investScenarioRepository;

    /**
     * ML에서 생성된 기본 시나리오 저장
     */
    public String createDefaultScenario(DefaultScenarioRequest request) {
        try {
            // 백엔드에서 설정하는 값들
            UUID scenarioId = UUID.randomUUID();
            UUID childId = UUID.fromString("c1111111-2222-3333-4444-555555555555"); // 임시 childId
            LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));

            // InvestScenario 객체 생성
            InvestScenario scenario = new InvestScenario(
                    scenarioId,
                    childId,
                    request.getStory(),
                    request.getIsCustom(),
                    now,        // createdAt - 생성 시간
                    null,       // updatedAt - 생성 시에는 null
                    null,       // investChapter - chapterId로 연결할 수도 있음
                    new ArrayList<>() // investSessions
            );

            investScenarioRepository.save(scenario);

            return "✅ 시나리오가 성공적으로 저장되었습니다. ID: " + scenarioId;

        } catch (Exception e) {
            throw new RuntimeException("시나리오 저장 실패: " + e.getMessage());
        }
    }

    /**
     * ML에서 생성된 커스텀 시나리오로 특정 챕터의 가장 오래된 시나리오 업데이트
     */
    public String updateOldestScenario(CustomScenarioRequest request) {
        try {
            // 특정 chapterId 중에서 업데이트되지 않은 것 중 가장 오래된 시나리오 찾기
            InvestScenario oldestScenario = investScenarioRepository
                    .findTopByInvestChapter_ChapterIdAndUpdatedAtIsNullOrderByCreateAtAsc(request.getChapterId());

            if (oldestScenario == null) {
                throw new RuntimeException("챕터 " + request.getChapterId() + "에서 업데이트할 시나리오가 없습니다.");
            }

            // 시나리오 업데이트
            LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));

            InvestScenario updatedScenario = new InvestScenario(
                    oldestScenario.getScenarioId(),
                    oldestScenario.getChildId(),
                    request.getStory(),                      // 새로운 story
                    request.getIsCustom(),                   // 새로운 isCustom
                    oldestScenario.getCreateAt(),            // 기존 createdAt 유지
                    now,                                     // updatedAt을 현재 시간으로 설정
                    oldestScenario.getInvestChapter(),
                    oldestScenario.getInvestSessions()
            );

            investScenarioRepository.save(updatedScenario);

            return "✅ 챕터 " + request.getChapterId() + "의 가장 오래된 시나리오가 업데이트되었습니다. ID: " + oldestScenario.getScenarioId();

        } catch (Exception e) {
            throw new RuntimeException("시나리오 업데이트 실패: " + e.getMessage());
        }
    }
}