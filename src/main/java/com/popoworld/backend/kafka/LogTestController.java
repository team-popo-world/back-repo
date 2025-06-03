package com.popoworld.backend.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class LogTestController {

    private final KafkaLogProducer kafkaLogProducer;

    @PostMapping("/log-test")
    public ResponseEntity<String> sendJsonLog() {
        LogMessage log = new LogMessage(null, "child001", "mission", "미션 성공");
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        try {
            String json = mapper.writeValueAsString(log);
            System.out.println("✅ 보낼 메시지: " + json); // 디버깅 로그
            kafkaLogProducer.sendLog("log-emotion", json);
            return ResponseEntity.ok("✅ 전송 완료");
        } catch (Exception e) {
            e.printStackTrace(); // 서버 로그 확인을 위해
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("❌ 전송 실패");
        }
    }



}