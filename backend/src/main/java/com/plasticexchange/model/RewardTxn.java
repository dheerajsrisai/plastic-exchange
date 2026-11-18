package com.plasticexchange.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor
@Document(collection = "rewards")
public class RewardTxn {
    @Id private String id;
    private String userId;
    private int points;
    private String reason;
    private LocalDateTime createdAt = LocalDateTime.now();
}
