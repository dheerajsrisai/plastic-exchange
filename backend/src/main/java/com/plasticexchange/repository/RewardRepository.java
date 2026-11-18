package com.plasticexchange.repository;

import com.plasticexchange.model.RewardTxn;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface RewardRepository extends MongoRepository<RewardTxn, String> {
    List<RewardTxn> findByUserIdOrderByCreatedAtDesc(String userId);
}
