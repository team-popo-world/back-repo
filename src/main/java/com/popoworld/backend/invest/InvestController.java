package com.popoworld.backend.invest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.popoworld.backend.invest.entity.InvestHistory;
import com.popoworld.backend.invest.entity.InvestScenario;
import com.popoworld.backend.invest.entity.InvestSession;
import com.popoworld.backend.invest.investHistoryKafka.InvestHistoryKafkaProducer;
import com.popoworld.backend.invest.repository.InvestHistoryMongoRepository;
import com.popoworld.backend.invest.repository.InvestScenarioRepository;
import com.popoworld.backend.invest.repository.InvestSessionRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/invest")
@Tag(name="Invest", description = "모의투자 관련 API")
public class InvestController {

    @Autowired
    private InvestScenarioRepository investScenarioRepository;

    @Autowired
    private InvestHistoryMongoRepository investHistoryMongoRepository;
    @Autowired
    private InvestHistoryKafkaProducer investHistoryKafkaProducer;

    @Autowired
    private InvestSessionRepository investSessionRepository;

    @Operation(
            summary = "챕터별 스토리 조회",
            description = "chapterId로 JSON 형식의 스토리를 문자열로 반환"
    )
    @ApiResponse(responseCode = "200", description = "성공 (JSON 문자열 반환)")
    @ApiResponse(responseCode = "404", description = "해당 챕터 ID 없음")
    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    @GetMapping("/chapter")
    public ResponseEntity<String> getChapterData(@RequestParam UUID chapterId) {
        try {
            InvestScenario scenario = investScenarioRepository.findByInvestChapter_ChapterId(chapterId);
            if (scenario == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(scenario.getStory());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/chapter")
    public ResponseEntity<String> updateGameData(
            @RequestParam UUID chapterId,
            @RequestParam Integer turn,
            @RequestBody Map<String, Object> requestData) {

        try {
            // 백엔드에서 설정하는 임시 값들
            UUID investSessionId = UUID.fromString("a1111111-2222-3333-4444-555555555555");
            UUID childId = UUID.fromString("c1111111-2222-3333-4444-555555555555");

            // InvestHistory 객체 생성
            // LocalDateTime으로 직접 파싱
            String startedAtStr = (String) requestData.get("started_at");
            String endedAtStr = (String) requestData.get("ended_at");

            LocalDateTime startedAt = LocalDateTime.parse(startedAtStr);
            LocalDateTime endedAt = LocalDateTime.parse(endedAtStr);

            InvestHistory history = new InvestHistory(
                    UUID.randomUUID(),
                    investSessionId,
                    chapterId,
                    childId,
                    turn,
                    (Integer) requestData.get("risk_level"),
                    (Integer) requestData.get("current_point"),
                    (Integer) requestData.get("before_value"),
                    (Integer) requestData.get("current_value"),
                    (Integer) requestData.get("initial_value"),
                    (Integer) requestData.get("number_of_shares"),
                    (Integer) requestData.get("income"),
                    (String) requestData.get("transaction_type"),
                    (Integer) requestData.get("plus_click"),
                    (Integer) requestData.get("minus_click"),
                    startedAt,   // ⏰ 여기 추가
                    endedAt      // ⏰ 여기도 유지
            );

            // 카프카로 전송
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            String json = mapper.writeValueAsString(history);

            investHistoryKafkaProducer.sendInvestHistory("invest-history", json);

            return ResponseEntity.ok("✅ 투자 데이터가 카프카로 전송되었습니다.");

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("❌ 전송 실패: " + e.getMessage());
        }
    }

    @PostMapping("/clear/chapter")
    public ResponseEntity<String> clearChapter(
            @RequestParam UUID chapterId,
            @RequestBody Map<String, Object> requestData) {

        try {
            // chapterId로 InvestScenario 조회
            InvestScenario scenario = investScenarioRepository.findByInvestChapter_ChapterId(chapterId);

            if (scenario == null) {
                return ResponseEntity.badRequest().body("해당 챕터 시나리오를 찾을 수 없습니다.");
            }

            // 프론트에서 받은 데이터
            Boolean success = (Boolean) requestData.get("success");
            Integer profit = (Integer) requestData.get("profit");
            String startedAtString = (String) requestData.get("started_at");

            // started_at 문자열을 LocalDateTime으로 변환
            LocalDateTime startedAt = LocalDateTime.parse(startedAtString);
            // 한국 시간으로 현재 시간 계산
            LocalDateTime endedAt = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
            // InvestSession 객체 생성
            InvestSession session = new InvestSession(
                    UUID.randomUUID(),           // investSessionId
                    scenario.getChildId(),       // scenario에서 가져온 childId
                    chapterId,                   // URL에서 받은 chapterId
                    startedAt,                   // 프론트에서 받은 시간
                    endedAt,                     // 현재 시간
                    success,
                    profit,
                    scenario                     // 조회한 scenario 객체
            );

            // DB에 저장
            investSessionRepository.save(session);

            return ResponseEntity.ok("✅ 게임 세션이 성공적으로 저장되었습니다.");

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("❌ 저장 실패: " + e.getMessage());
        }
    }
}
