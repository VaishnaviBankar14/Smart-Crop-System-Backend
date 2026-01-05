package com.smartcrops.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.smartcrops.dto.ChatRequest;
import com.smartcrops.model.ChatMessage;
import com.smartcrops.model.User;
import com.smartcrops.repository.ChatRepository;
import com.smartcrops.repository.UserRepository;
import com.smartcrops.service.ChatService;
import com.smartcrops.jwt.JwtUtil;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private ChatService chatService;

    @Autowired
    private UserRepository userRepository;

    /**
     * 📩 Send a chat message and receive bot response
     */
    @PostMapping("/send")
    public ChatMessage sendChat(
            @RequestBody ChatRequest request,
            @RequestHeader("Authorization") String authHeader) {

        // Remove "Bearer "
        String token = authHeader.replace("Bearer ", "");

        // Extract email from JWT
        String email = JwtUtil.extractUsername(token);

        User user = userRepository.findByEmailIgnoreCase(email).orElseThrow();
        Long userId = user.getId();

        String reply = chatService.getReply(
                request.getMessage(),
                request.getLanguage()
        );

        ChatMessage chat = new ChatMessage();
        chat.setUserId(userId);
        chat.setUserMessage(request.getMessage());
        chat.setBotResponse(reply);
        chat.setLanguage(request.getLanguage());

        return chatRepository.save(chat);
    }

    /**
     * 📜 Fetch chat history for logged-in user
     */
    @GetMapping("/history")
    public List<ChatMessage> getChatHistory(
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.replace("Bearer ", "");

        String email = JwtUtil.extractUsername(token);

        User user = userRepository.findByEmailIgnoreCase(email).orElseThrow();
        Long userId = user.getId();

        return chatRepository.findByUserId(userId);
    }
}
