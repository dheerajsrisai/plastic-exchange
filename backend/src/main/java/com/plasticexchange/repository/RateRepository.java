package com.plasticexchange.repository;

import com.plasticexchange.model.RewardRate;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RateRepository extends MongoRepository<RewardRate, String> { }
