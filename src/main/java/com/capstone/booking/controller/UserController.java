package com.capstone.booking.controller;

import com.capstone.booking.model.User;
import com.capstone.booking.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * A small read-only endpoint so the frontend can offer a "book as..." picker.
 * (In a real system this would be replaced by proper authentication.)
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public record UserResponse(Long userId, String name, String email) {
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> listUsers() {
        List<UserResponse> users = userRepository.findAll().stream()
                .map(u -> new UserResponse(u.getUserId(), u.getName(), u.getEmail()))
                .toList();
        return ResponseEntity.ok(users);
    }
}
