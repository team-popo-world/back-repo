package com.popoworld.backend.tests;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/test-mission")
@RequiredArgsConstructor
public class TestRepository1 {

    private final TestRepository missionRepository;

    // ✅ 미션 저장 (GET → POST로 바꾸는 것도 추천)
    @PostMapping
    public Test1 saveMission(@RequestParam String childId, @RequestParam String title) {
        Test1 mission = Test1.builder()
                .childId(childId)
                .title(title)
                .completed(false)
                .build();
        return missionRepository.save(mission);
    }

    // ✅ 특정 자녀의 미션 리스트 조회
    @GetMapping("/{childId}")
    public List<Test1> getMissions(@PathVariable String childId) {
        return missionRepository.findByChildId(childId);
    }
}
