package com.popoworld.backend.kafka;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class KafkaElasticsearchConsumer {

    private final ElasticsearchClient elasticsearchClient;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "log-emotion", groupId = "es-log-group")
    public void consume(String message) {
        try {
            LogMessage log = objectMapper.readValue(message, LogMessage.class);

            elasticsearchClient.index(i -> i
                    .index("log_emotion_index") // 인덱스 이름
                    .id(UUID.randomUUID().toString()) // ID 지정
                    .document(log) // 저장할 객체
            );

            System.out.println("✅ Elasticsearch에 저장 완료: " + log);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
