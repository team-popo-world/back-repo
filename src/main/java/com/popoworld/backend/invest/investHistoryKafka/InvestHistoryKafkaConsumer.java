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
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());

            InvestHistory history = objectMapper.readValue(message, InvestHistory.class);
            investHistoryMongoRepository.save(history);
            System.out.println("✅ InvestHistory MongoDB 저장 완료: " + history.getId());
        } catch (Exception e) {
            System.err.println("❌ InvestHistory 메시지 파싱 실패: " + message);
            e.printStackTrace();
        }
    }

}
