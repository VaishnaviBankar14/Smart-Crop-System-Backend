package com.smartcrops.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import com.smartcrops.model.RecommendationHistory;

public interface RecommendationHistoryRepository
        extends JpaRepository<RecommendationHistory, Long> {

    List<RecommendationHistory> findByUserEmail(String userEmail);
}
