package com.smartcrops.controller;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.smartcrops.jwt.JwtUtil;
import com.smartcrops.model.User;
import com.smartcrops.repository.UserRepository;
import com.smartcrops.service.PasswordResetUtil;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    // ---------- REGISTER API ----------
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {

        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email already registered");
        }

        // 🔐 ENCRYPT PASSWORD
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("USER");

        userRepository.save(user);

        return ResponseEntity.ok("User registered successfully");
    }

    // ---------- LOGIN API ----------
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody User loginData) {

        Optional<User> userOpt = userRepository.findByEmail(loginData.getEmail());

        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid email or password");
        }

        User user = userOpt.get();

        if (!passwordEncoder.matches(loginData.getPassword(), user.getPassword())) {
            return ResponseEntity.badRequest().body("Invalid email or password");
        }

        // 🔐 GENERATE JWT TOKEN
        String token = JwtUtil.generateToken(
                user.getEmail(),
                user.getRole()
        );



        return ResponseEntity.ok(token);
    }
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email not registered"));

        String token = PasswordResetUtil.generateToken();

        user.setResetToken(token);
        user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);

        System.out.println(
          "RESET LINK: http://localhost:3000/reset-password?token=" + token
        );

        return ResponseEntity.ok("Reset link sent");
    }
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(
            @RequestParam String token,
            @RequestParam String newPassword) {

        User user = userRepository.findByResetToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        if (user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("Token expired");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);

        return ResponseEntity.ok("Password reset successful");
    }


}
