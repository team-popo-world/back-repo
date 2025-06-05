package com.popoworld.backend.invest.investHistoryKafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.popoworld.backend.invest.entity.InvestHistory;
import com.popoworld.backend.invest.repository.InvestHistoryMongoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InvestHistoryKafkaConsumer {
    private final InvestHistoryMongoRepository investHistoryMongoRepository;

    @KafkaListener(topics = "invest-history", groupId = "invest-consumer-group")
    public void consume(String message) {
        System.out.println("🔍 Kafka 메시지 수신: " + message);

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());

            InvestHistory history = objectMapper.readValue(message, InvestHistory.class);
            System.out.println("📝 파싱된 데이터: " + history.toString());

            InvestHistory savedHistory = investHistoryMongoRepository.save(history);
            System.out.println("✅ MongoDB 저장 완료 - ID: " + savedHistory.getId());
            System.out.println("📊 저장된 데이터 확인: " + savedHistory);

        } catch (Exception e) {
            System.err.println("❌ 에러 발생 - 메시지: " + message);
            System.err.println("❌ 에러 내용: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
