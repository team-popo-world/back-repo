package com.popoworld.backend.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "log_messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogMessage {
    @Id

    private String id;
    private String userId;
    private String type;
    private String message;

}
