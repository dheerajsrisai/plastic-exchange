package com.plasticexchange.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data @NoArgsConstructor @AllArgsConstructor
@Document(collection = "reward_rates")
public class RewardRate {
    @Id private String type;
    private int pointsPerKg;
}
