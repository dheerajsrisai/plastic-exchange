package com.plasticexchange.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor
@Document(collection = "redemptions")
public class Redemption {
    @Id private String id;
    private String userId;
    private String item;
    private int pointsUsed;
    private LocalDateTime createdAt = LocalDateTime.now();
}
