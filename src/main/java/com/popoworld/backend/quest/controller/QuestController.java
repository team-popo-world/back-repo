package com.popoworld.backend.quest.controller;

import com.popoworld.backend.quest.dto.ParentQuestRequest;
import com.popoworld.backend.quest.dto.QuestResponse;
import com.popoworld.backend.quest.dto.QuestStateChangeRequest;
import com.popoworld.backend.quest.service.QuestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/quest")
@RequiredArgsConstructor
@Tag(name="Quest", description = "퀘스트 관리 API")
public class QuestController {
    private final QuestService questService;

    @GetMapping
    @Operation(
            summary = "퀘스트 목록 조회",
            description = "퀘스트 목록을 조회합니다. 타입으로 필터링 가능합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "퀘스트 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 파라미터"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    public ResponseEntity<List<QuestResponse>> getQuests(
            @Parameter(description = "퀘스트 타입 (parent, daily)", example = "daily")
            @RequestParam(required = false) String type
    ){
        try{
            //JWT에서 childId 추출 예정, 현재는 임시값!
            UUID childId = UUID.fromString("c1111111-2222-3333-4444-555555555555");
            List<QuestResponse>quests = questService.getQuestsByType(childId,type);
            return ResponseEntity.ok(quests);
        }catch (IllegalArgumentException e){
            //잘못된 type enum값이 들어온 경우
            return ResponseEntity.badRequest().build();
        }catch (Exception e){
            //기타 예외
            return ResponseEntity.internalServerError().build();
        }
    }


    //테스트용 api. 신규 가입자 일일퀘스트는 자정에 생성하는게 아니라 가입하자마자 생성해야되니까
    //그거 테스트 하는 api임. 나중에 삭제될거
    @PostMapping("/test/create-for-new-child")
    public ResponseEntity<String> testCreateForNewChild(
            @RequestParam String childId
    ) {
        UUID uuid = UUID.fromString(childId);
        questService.createDailyQuestsForNewChild(uuid);  // 🎯 이 기능 테스트
        return ResponseEntity.ok("✅ 아이 ID [" + childId + "]에게 일일퀘스트 5개 생성 완료!");
    }

    // 새로 추가되는 상태 변경 API
    @PostMapping("/state")
    @Operation(
            summary = "퀘스트 상태 변경",
            description = "퀘스트의 상태를 변경합니다. (수락, 완료, 승인, 보상받기)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상태 변경 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 또는 상태 변경 규칙 위반"),
            @ApiResponse(responseCode = "404", description = "퀘스트를 찾을 수 없음")
    })
    public ResponseEntity<String> changeQuestState(@RequestBody QuestStateChangeRequest request){
        try{
            questService.changeQuestState(request);
            return ResponseEntity.ok("퀘스트 상태가 변경되었습니다.");
        }catch (IllegalArgumentException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("서버 오류가 발생했습니다.");
        }

    }
    // 부모 퀘스트 생성 API (새로 추가)
    @PostMapping("/create")
    @Operation(
            summary = "부모 퀘스트 생성",
            description = "부모가 아이에게 새로운 퀘스트를 생성합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "퀘스트 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<QuestResponse> createParentQuest(@RequestBody ParentQuestRequest request) {
        try {
            QuestResponse createdQuest = questService.createParentQuest(request);
            return ResponseEntity.ok(createdQuest);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
