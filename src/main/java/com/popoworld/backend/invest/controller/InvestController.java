package com.popoworld.backend.invest.controller;

import com.popoworld.backend.invest.dto.request.ChapterRequest;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/invest")
@Tag(name="Invest", description = "모의투자 관련 API")
public class InvestController {

    @Autowired
    private InvestHistoryKafkaProducer investHistoryKafkaProducer;

    private final InvestService investService;

    @PostMapping("/chapter")
    @Operation(
            summary = "챕터별 스토리 조회 및 게임 세션 시작",
            description = "chapterId로 JSON 형식의 스토리를 반환하고 새로운 게임 세션을 생성"
    )
    @ApiResponse(responseCode = "200", description = "성공 (JSON 문자열 + 세션 ID 반환)")
    @ApiResponse(responseCode = "404", description = "해당 챕터 ID 없음")
    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    public ResponseEntity<ChapterDataResponse> getChapterData(@RequestBody ChapterRequest request) {
        try {
            ChapterDataResponse response = investService.getChapterDataAndCreateSession(request.getChapterId());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/chapter/turn")
    @Operation(
            summary = "게임 턴 정보 업데이트",
            description = "게임 진행 중 각 턴의 투자 정보를 카프카를 통해 MongoDB에 저장"
    )
    @ApiResponse(responseCode = "200", description = "성공 (카프카로 데이터 전송 완료)")
    @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터")
    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    public ResponseEntity<String> updateGameData(@RequestBody TurnDataRequest request) {

        try {
            TurnDataResponse response = investService.updateGameData(request.getChapterId(), request.getTurn(), request);
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
            @RequestBody ClearChapterRequest request) {
        try {
            ClearChapterResponse response = investService.clearChapter(request.getChapterId(), request);
            return ResponseEntity.ok(response.getMessage()); // String 형태로 반환 (기존과 동일)
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("해당 게임 세션을 찾을 수 없습니다.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("❌ 업데이트 실패: " + e.getMessage());
        }
    }


}