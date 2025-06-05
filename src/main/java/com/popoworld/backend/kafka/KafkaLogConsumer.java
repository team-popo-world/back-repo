package com.popoworld.backend.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaLogConsumer {

    private final LogMessageRepository logMessageRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = "log-emotion", groupId = "log-consumer-group")
    public void consume(String message) {
        try {
            LogMessage log = objectMapper.readValue(message, LogMessage.class);
            logMessageRepository.save(log);
            System.out.println("✅ MongoDB에 저장 완료: " + log);
        } catch (Exception e) {
            System.err.println("❌ 메시지 파싱 실패: " + message);
            e.printStackTrace();
        }
    }
}