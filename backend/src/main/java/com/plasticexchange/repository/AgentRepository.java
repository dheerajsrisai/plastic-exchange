package com.plasticexchange.repository;

import com.plasticexchange.model.Agent;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface AgentRepository extends MongoRepository<Agent, String> {
    Optional<Agent> findByUserId(String userId);
    List<Agent> findByApprovedTrueAndAvailableTrue();
}
