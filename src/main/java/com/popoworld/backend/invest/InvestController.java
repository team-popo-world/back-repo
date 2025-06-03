package com.popoworld.backend.invest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.popoworld.backend.invest.entity.InvestHistory;
import com.popoworld.backend.invest.entity.InvestScenario;
import com.popoworld.backend.invest.entity.InvestSession;
import com.popoworld.backend.invest.investHistoryKafka.InvestHistoryKafkaProducer;
import com.popoworld.backend.invest.repository.InvestChapterRepository;
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
import java.util.ArrayList;
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

    @Autowired
    private InvestChapterRepository investChapterRepository;

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
    @Operation(
            summary = "게임 턴 정보 업데이트",
            description = "게임 진행 중 각 턴의 투자 정보를 카프카를 통해 MongoDB에 저장"
    )
    @ApiResponse(responseCode = "200", description = "성공 (카프카로 데이터 전송 완료)")
    @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터")
    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
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
    @Operation(
            summary = "게임 종료 또는 클리어 정보 저장",
            description = "게임 완료 시 성공 여부, 수익률, 시작/종료 시간을 InvestSession 테이블에 저장"
    )
    @ApiResponse(responseCode = "200", description = "성공 (게임 세션 저장 완료)")
    @ApiResponse(responseCode = "400", description = "해당 챕터 시나리오를 찾을 수 없음")
    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
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

    @PostMapping("/scenario")
    @Operation(
            summary = "ML에서 생성된 시나리오 저장",
            description = "ML에서 생성된 시나리오 데이터와 커스텀 여부를 받아서 InvestScenario 테이블에 저장"
    )
    @ApiResponse(responseCode = "200", description = "성공 (시나리오 저장 완료)")
    @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터")
    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    public ResponseEntity<String> createScenario(
            @RequestParam UUID chapterId,
            @RequestBody Map<String, Object> requestData) {

        try {
            // ML에서 받은 데이터
            String story = (String) requestData.get("story");
            Boolean isCustom = (Boolean) requestData.get("isCustom");

            // 백엔드에서 설정하는 값들
            UUID scenarioId = UUID.randomUUID();
            UUID childId = UUID.fromString("c1111111-2222-3333-4444-555555555555");
            LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));

            // InvestScenario 객체 생성
            InvestScenario scenario = new InvestScenario(
                    scenarioId,
                    childId,
                    story,
                    isCustom,
                    now,        // createdAt - 생성 시간
                    null,       // updatedAt - 생성 시에는 null
                    null,
                    new ArrayList<>()
            );

            investScenarioRepository.save(scenario);

            return ResponseEntity.ok("✅ 시나리오가 성공적으로 저장되었습니다. ID: " + scenarioId);

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("❌ 저장 실패: " + e.getMessage());
        }
    }

    @PutMapping("/scenario/update")
    @Operation(
            summary = "가장 오래된 시나리오 업데이트",
            description = "ML에서 받은 데이터로 가장 오래된 시나리오를 업데이트하고 updatedAt을 현재 시간으로 설정"
    )
    @ApiResponse(responseCode = "200", description = "성공 (시나리오 업데이트 완료)")
    @ApiResponse(responseCode = "404", description = "업데이트할 시나리오가 없음")
    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    public ResponseEntity<String> updateOldestScenario(
            @RequestBody Map<String, Object> requestData) {

        try {
            // ML에서 받은 데이터
            String story = (String) requestData.get("story");
            Boolean isCustom = (Boolean) requestData.get("isCustom");

            // 가장 오래된 시나리오 찾기 (createdAt 기준 오름차순 정렬)
            InvestScenario oldestScenario = investScenarioRepository.findTopByOrderByCreateAtAsc();

            if (oldestScenario == null) {
                return ResponseEntity.notFound().build();
            }

            // 시나리오 업데이트
            LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));

            InvestScenario updatedScenario = new InvestScenario(
                    oldestScenario.getScenarioId(),  // 기존 ID 유지
                    oldestScenario.getChildId(),     // 기존 childId 유지
                    story,                           // 새로운 story
                    isCustom,                        // 새로운 isCustom
                    oldestScenario.getCreateAt(),    // 기존 createdAt 유지
                    now,                             // updatedAt을 현재 시간으로 설정
                    oldestScenario.getInvestChapter(), // 기존 관계 유지
                    oldestScenario.getInvestSessions() // 기존 관계 유지
            );

            investScenarioRepository.save(updatedScenario);

            return ResponseEntity.ok("✅ 가장 오래된 시나리오가 업데이트되었습니다. ID: " + oldestScenario.getScenarioId());

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("❌ 업데이트 실패: " + e.getMessage());
        }
    }

}
