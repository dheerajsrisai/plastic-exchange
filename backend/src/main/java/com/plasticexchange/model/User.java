package com.plasticexchange.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor
@Document(collection = "users")
public class User {
    @Id private String id;
    private String name;
    @Indexed(unique = true) private String email;
    private String passwordHash;
    private Role role;
    private String phone;
    private String address;
    private String area;
    private int rewardPoints;
    private LocalDateTime createdAt = LocalDateTime.now();
}
