package com.popoworld.backend.invest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.popoworld.backend.invest.entity.InvestHistory;
import com.popoworld.backend.invest.entity.InvestScenario;
import com.popoworld.backend.invest.entity.InvestSession;
import com.popoworld.backend.invest.investHistoryKafka.InvestHistoryKafkaProducer;
import com.popoworld.backend.invest.repository.InvestScenarioRepository;
import com.popoworld.backend.invest.repository.InvestSessionRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/invest")
@Tag(name="Invest", description = "모의투자 관련 API")
public class InvestController {

    @Autowired
    private InvestScenarioRepository investScenarioRepository;


    @Autowired
    private InvestHistoryKafkaProducer investHistoryKafkaProducer;

    @Autowired
    private InvestSessionRepository investSessionRepository;


    @GetMapping("/chapter")
    @Operation(
            summary = "챕터별 스토리 조회 및 게임 세션 시작",
            description = "chapterId로 JSON 형식의 스토리를 반환하고 새로운 게임 세션을 생성"
    )
    @ApiResponse(responseCode = "200", description = "성공 (JSON 문자열 + 세션 ID 반환)")
    @ApiResponse(responseCode = "404", description = "해당 챕터 ID 없음")
    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    public ResponseEntity<Map<String, Object>> getChapterData(@RequestParam UUID chapterId) {
        try {
            InvestScenario scenario = investScenarioRepository.findByInvestChapter_ChapterId(chapterId);
            if (scenario == null) {
                return ResponseEntity.notFound().build();
            }

            // 새로운 게임 세션 생성
            UUID sessionId = UUID.randomUUID();
            UUID childId = UUID.fromString("c1111111-2222-3333-4444-555555555555"); // 임시 childId
            LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));

            InvestSession newSession = new InvestSession(
                    sessionId,        // 새로 생성한 세션 ID
                    childId,          // 임시 childId
                    chapterId,        // URL에서 받은 chapterId
                    now,              // startedAt - 현재 시간
                    null,             // endedAt - 아직 게임이 안 끝남
                    null,             // success - 아직 모름
                    null,             // profit - 아직 모름
                    scenario          // 조회한 scenario 객체
            );

            // 세션 저장
            investSessionRepository.save(newSession);

            // 응답 데이터 구성
            Map<String, Object> response = new HashMap<>();
            response.put("sessionId", sessionId.toString());
            response.put("story", scenario.getStory());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    //검토 안했음. 카프카 몽고 로그데이터라 나중에 검토하기
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
            // 프론트에서 받은 sessionId
            String sessionIdStr = (String) requestData.get("sessionId");
            UUID investSessionId = UUID.fromString(sessionIdStr);

            // 임시 childId (나중에 JWT에서 가져올 예정)
            UUID childId = UUID.fromString("c1111111-2222-3333-4444-555555555555");

            // InvestHistory 객체 생성
            // LocalDateTime으로 직접 파싱
            String startedAtStr = (String) requestData.get("started_at");
            String endedAtStr = (String) requestData.get("ended_at");

            LocalDateTime startedAt = LocalDateTime.parse(startedAtStr);
            LocalDateTime endedAt = LocalDateTime.parse(endedAtStr);

            InvestHistory history = new InvestHistory(
                    UUID.randomUUID(),
                    investSessionId,     // 프론트에서 받은 sessionId 사용
                    chapterId,
                    childId,
                    turn,
                    (String) requestData.get("risk_level"),
                    (Integer) requestData.get("current_point"),
                    (Integer) requestData.get("before_value"),
                    (Integer) requestData.get("current_value"),
                    (Integer) requestData.get("initial_value"),
                    (Integer) requestData.get("number_of_shares"),
                    (Integer) requestData.get("income"),
                    (String) requestData.get("transaction_type"),
                    (Integer) requestData.get("plus_click"),
                    (Integer) requestData.get("minus_click"),
                    startedAt,
                    endedAt
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
            description = "게임 완료 시 기존 세션의 성공 여부, 수익률, 종료 시간을 업데이트"
    )
    @ApiResponse(responseCode = "200", description = "성공 (게임 세션 업데이트 완료)")
    @ApiResponse(responseCode = "400", description = "해당 세션을 찾을 수 없음")
    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    public ResponseEntity<String> clearChapter(
            @RequestParam UUID chapterId,
            @RequestBody Map<String, Object> requestData) {

        try {
            // 프론트에서 받은 데이터
            String sessionIdStr = (String) requestData.get("sessionId");
            UUID sessionId = UUID.fromString(sessionIdStr);
            Boolean success = (Boolean) requestData.get("success");
            Integer profit = (Integer) requestData.get("profit");

            // sessionId로 기존 세션 찾기
            InvestSession existingSession = investSessionRepository.findById(sessionId).orElse(null);

            if (existingSession == null) {
                return ResponseEntity.badRequest().body("해당 게임 세션을 찾을 수 없습니다.");
            }

            // 기존 세션 업데이트
            LocalDateTime endedAt = LocalDateTime.now(ZoneId.of("Asia/Seoul"));

            InvestSession updatedSession = new InvestSession(
                    existingSession.getInvestSessionId(),  // 기존 세션 ID 유지
                    existingSession.getChildId(),          // 기존 childId 유지
                    existingSession.getChapterId(),        // 기존 chapterId 유지
                    existingSession.getStartedAt(),        // 기존 시작 시간 유지
                    endedAt,                               // 종료 시간은 현재 시간
                    success,                               // 프론트에서 받은 성공 여부
                    profit,                                // 프론트에서 받은 수익률
                    existingSession.getInvestScenario()    // 기존 scenario 유지
            );

            // 업데이트된 세션 저장
            investSessionRepository.save(updatedSession);

            return ResponseEntity.ok("✅ 게임 세션이 성공적으로 업데이트되었습니다.");

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("❌ 업데이트 실패: " + e.getMessage());
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
    public ResponseEntity<String> updateOldestScenario(
            @RequestBody Map<String, Object> requestData) {

        try {
            // ML에서 받은 데이터
            String story = (String) requestData.get("story");
            Boolean isCustom = (Boolean) requestData.get("isCustom");

            // 업데이트되지 않은 것 중에서 가장 오래된 시나리오 찾기
            InvestScenario oldestScenario = investScenarioRepository.findTopByUpdatedAtIsNullOrderByCreateAtAsc();

            if (oldestScenario == null) {
                return ResponseEntity.badRequest().body("업데이트할 시나리오가 없습니다. 모든 시나리오가 이미 업데이트되었습니다.");
            }

            // 시나리오 업데이트
            LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));

            InvestScenario updatedScenario = new InvestScenario(
                    oldestScenario.getScenarioId(),
                    oldestScenario.getChildId(),
                    story,                           // 새로운 story
                    isCustom,                        // 새로운 isCustom
                    oldestScenario.getCreateAt(),    // 기존 createdAt 유지
                    now,                             // updatedAt을 현재 시간으로 설정
                    oldestScenario.getInvestChapter(),
                    oldestScenario.getInvestSessions()
            );

            investScenarioRepository.save(updatedScenario);

            return ResponseEntity.ok("✅ 가장 오래된 미업데이트 시나리오가 업데이트되었습니다. ID: " + oldestScenario.getScenarioId());

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("❌ 업데이트 실패: " + e.getMessage());
        }
    }

}
