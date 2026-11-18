package com.plasticexchange.repository;

import com.plasticexchange.model.Redemption;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface RedemptionRepository extends MongoRepository<Redemption, String> {
    List<Redemption> findByUserIdOrderByCreatedAtDesc(String userId);
}
