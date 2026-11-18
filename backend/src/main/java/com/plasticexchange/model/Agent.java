package com.plasticexchange.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data @NoArgsConstructor @AllArgsConstructor
@Document(collection = "agents")
public class Agent {
    @Id private String id;
    private String userId;
    private String name;
    private String phone;
    private String vehicleNo;
    private String area;
    private boolean available = true;
    private boolean approved = false;
    private double rating = 5.0;
}
