package com.popoworld.backend.invest.controller;

import com.popoworld.backend.invest.dto.request.CustomScenarioRequest;
import com.popoworld.backend.invest.dto.request.DefaultScenarioRequest;
import com.popoworld.backend.invest.service.ScenarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/scenario")
@Tag(name="ML Scenario", description = "ML에서 생성된 시나리오 관리 API")
public class ScenarioController {
        private final ScenarioService scenarioService;

        @PostMapping("/default")
        @Operation(
                summary = "ML에서 생성된 기본 시나리오 저장",
                description = "ML에서 생성된 시나리오 데이터와 커스텀 여부를 받아서 InvestScenario 테이블에 저장"
        )
        @ApiResponse(responseCode = "200", description = "성공 (시나리오 저장 완료)")
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터")
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
        public ResponseEntity<String> createDefaultScenario(@RequestBody DefaultScenarioRequest request) {
            try {
                String result = scenarioService.createDefaultScenario(request);
                return ResponseEntity.ok(result);
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().body("❌ 저장 실패: " + e.getMessage());
            } catch (Exception e) {
                return ResponseEntity.internalServerError().body("❌ 서버 오류: " + e.getMessage());
            }
        }

        @PutMapping("/parent")
        @Operation(
                summary = "ML 커스텀 시나리오로 기존 시나리오 업데이트",
                description = "특정 챕터에서 업데이트되지 않은 것 중 가장 오래된 시나리오를 ML에서 받은 커스텀 데이터로 업데이트"
        )
        @ApiResponse(responseCode = "200", description = "성공 (시나리오 업데이트 완료)")
        @ApiResponse(responseCode = "400", description = "업데이트할 시나리오가 없음")
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
        public ResponseEntity<String> updateOldestScenario(@RequestBody CustomScenarioRequest request) {
            try {
                String result = scenarioService.updateOldestScenario(request);
                return ResponseEntity.ok(result);
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().body("❌ 업데이트 실패: " + e.getMessage());
            } catch (Exception e) {
                return ResponseEntity.internalServerError().body("❌ 서버 오류: " + e.getMessage());
            }
        }
}
