package com.plasticexchange.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor
@Document(collection = "pickups")
public class Pickup {
    @Id private String id;
    private String userId;
    private String userName;
    private String agentId;
    private String agentName;
    private String plasticType;
    private String aiType;
    private double aiConfidence;
    private boolean needsReview;
    private double estimatedWeightKg;
    private double weightKg;
    private String imageUrl;
    private String address;
    private String area;
    private PickupStatus status = PickupStatus.REQUESTED;
    private LocalDateTime scheduledAt;
    private int pointsAwarded;
    private LocalDateTime createdAt = LocalDateTime.now();
}
