package com.popoworld.backend.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaLogProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendLog(String topic, String message) {
        kafkaTemplate.send(topic, message);
    }

}

