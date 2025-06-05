package com.popoworld.backend.invest.controller;

import com.popoworld.backend.invest.dto.request.ClearChapterRequest;
import com.popoworld.backend.invest.dto.request.TurnDataRequest;
import com.popoworld.backend.invest.dto.response.ChapterDataResponse;
import com.popoworld.backend.invest.dto.response.ClearChapterResponse;
import com.popoworld.backend.invest.dto.response.TurnDataResponse;
import com.popoworld.backend.invest.investHistoryKafka.InvestHistoryKafkaProducer;
import com.popoworld.backend.invest.service.InvestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/invest")
@Tag(name="Invest", description = "모의투자 관련 API")
public class InvestController {

    @Autowired
    private InvestHistoryKafkaProducer investHistoryKafkaProducer;

    private final InvestService investService;

    @GetMapping("/chapter")
    @Operation(
            summary = "챕터별 스토리 조회 및 게임 세션 시작",
            description = "chapterId로 JSON 형식의 스토리를 반환하고 새로운 게임 세션을 생성"
    )
    @ApiResponse(responseCode = "200", description = "성공 (JSON 문자열 + 세션 ID 반환)")
    @ApiResponse(responseCode = "404", description = "해당 챕터 ID 없음")
    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    public ResponseEntity<ChapterDataResponse> getChapterData(@RequestParam UUID chapterId) {
        try {
            ChapterDataResponse response = investService.getChapterDataAndCreateSession(chapterId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
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
            @RequestBody TurnDataRequest request) {

        try {
            TurnDataResponse response = investService.updateGameData(chapterId, turn, request);
            return ResponseEntity.ok(response.getMessage()); // String 형태로 반환 (기존과 동일)
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError()
                    .body("❌ 전송 실패: " + e.getMessage());
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
            @RequestBody ClearChapterRequest request) {
        try {
            ClearChapterResponse response = investService.clearChapter(chapterId, request);
            return ResponseEntity.ok(response.getMessage()); // String 형태로 반환 (기존과 동일)
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("해당 게임 세션을 찾을 수 없습니다.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("❌ 업데이트 실패: " + e.getMessage());
        }
    }



    
//    @PostMapping("/scenario")
//    @Operation(
//            summary = "ML에서 생성된 시나리오 저장",
//            description = "ML에서 생성된 시나리오 데이터와 커스텀 여부를 받아서 InvestScenario 테이블에 저장"
//    )
//    @ApiResponse(responseCode = "200", description = "성공 (시나리오 저장 완료)")
//    @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터")
//    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
//    public ResponseEntity<String> createScenario(
//            @RequestParam UUID chapterId,
//            @RequestBody Map<String, Object> requestData) {
//
//        try {
//            // ML에서 받은 데이터
//            String story = (String) requestData.get("story");
//            Boolean isCustom = (Boolean) requestData.get("isCustom");
//
//            // 백엔드에서 설정하는 값들
//            UUID scenarioId = UUID.randomUUID();
//            UUID childId = UUID.fromString("c1111111-2222-3333-4444-555555555555");
//            LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
//
//            // InvestScenario 객체 생성
//            InvestScenario scenario = new InvestScenario(
//                    scenarioId,
//                    childId,
//                    story,
//                    isCustom,
//                    now,        // createdAt - 생성 시간
//                    null,       // updatedAt - 생성 시에는 null
//                    null,
//                    new ArrayList<>()
//            );
//
//            investScenarioRepository.save(scenario);
//
//            return ResponseEntity.ok("✅ 시나리오가 성공적으로 저장되었습니다. ID: " + scenarioId);
//
//        } catch (Exception e) {
//            return ResponseEntity.internalServerError()
//                    .body("❌ 저장 실패: " + e.getMessage());
//        }
//    }
//
//    @PutMapping("/scenario/update")
//    public ResponseEntity<String> updateOldestScenario(
//            @RequestBody Map<String, Object> requestData) {
//
//        try {
//            // ML에서 받은 데이터
//            String story = (String) requestData.get("story");
//            Boolean isCustom = (Boolean) requestData.get("isCustom");
//
//            // 업데이트되지 않은 것 중에서 가장 오래된 시나리오 찾기
//            InvestScenario oldestScenario = investScenarioRepository.findTopByUpdatedAtIsNullOrderByCreateAtAsc();
//
//            if (oldestScenario == null) {
//                return ResponseEntity.badRequest().body("업데이트할 시나리오가 없습니다. 모든 시나리오가 이미 업데이트되었습니다.");
//            }
//
//            // 시나리오 업데이트
//            LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
//
//            InvestScenario updatedScenario = new InvestScenario(
//                    oldestScenario.getScenarioId(),
//                    oldestScenario.getChildId(),
//                    story,                           // 새로운 story
//                    isCustom,                        // 새로운 isCustom
//                    oldestScenario.getCreateAt(),    // 기존 createdAt 유지
//                    now,                             // updatedAt을 현재 시간으로 설정
//                    oldestScenario.getInvestChapter(),
//                    oldestScenario.getInvestSessions()
//            );
//
//            investScenarioRepository.save(updatedScenario);
//
//            return ResponseEntity.ok("✅ 가장 오래된 미업데이트 시나리오가 업데이트되었습니다. ID: " + oldestScenario.getScenarioId());
//
//        } catch (Exception e) {
//            return ResponseEntity.internalServerError()
//                    .body("❌ 업데이트 실패: " + e.getMessage());
//        }
//    }

}
