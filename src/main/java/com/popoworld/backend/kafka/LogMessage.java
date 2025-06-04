package com.popoworld.backend.kafka;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.util.UUID;

@Document(collection = "log_messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogMessage {
    @Id
    private UUID id;
    private String userId;
    private String type;
    private String message;

}
