package com.plasticexchange.repository;

import com.plasticexchange.model.Pickup;
import com.plasticexchange.model.PickupStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface PickupRepository extends MongoRepository<Pickup, String> {
    List<Pickup> findByUserIdOrderByCreatedAtDesc(String userId);
    List<Pickup> findByAgentIdOrderByCreatedAtDesc(String agentId);
    List<Pickup> findByStatus(PickupStatus status);
    long countByAgentIdAndStatus(String agentId, PickupStatus status);
    List<Pickup> findAllByOrderByCreatedAtDesc();
}
