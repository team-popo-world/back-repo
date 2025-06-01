package com.popoworld.backend.kafka;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

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
