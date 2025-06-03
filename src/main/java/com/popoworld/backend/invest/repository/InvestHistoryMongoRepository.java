package com.popoworld.backend.invest.repository;

import com.popoworld.backend.invest.entity.InvestHistory;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface InvestHistoryMongoRepository extends MongoRepository<InvestHistory, UUID> {
}
