package com.smartcrops.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smartcrops.model.ChatMessage;

@Repository
public interface ChatRepository extends JpaRepository<ChatMessage, Long> {

    // 🔍 Fetch chat history for a specific user
    List<ChatMessage> findByUserId(Long userId);

}
