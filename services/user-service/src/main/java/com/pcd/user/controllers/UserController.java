package com.pcd.user.controllers;

import com.pcd.user.model.User;
import com.pcd.user.records.UserRequest;
import com.pcd.user.records.UserResponse;
import com.pcd.user.service.UserService;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService service;

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody @Valid UserRequest request) {
        return ResponseEntity.ok(service.createUser(request));
    }

    @PutMapping
    public ResponseEntity<Void> updateUser(@RequestBody @Valid UserRequest request) {
        service.updateUser(request);
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/get-all")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(service.getAllUsers());
    }

    @GetMapping("/exists/{id}")
    public ResponseEntity<Boolean> existsById(@PathVariable("id") String id) {
        return ResponseEntity.ok(service.existsById(id));
    }

    @GetMapping("/get-user-by-id/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable("id") String id) {
        return ResponseEntity.ok(service.getUserById(id));
    }

    @GetMapping("/by-email/{email}")
    public ResponseEntity<User> getUserByEmail(@PathVariable String email) {
        return service.getByEmail(email);
    }

    @DeleteMapping("/delete-user-by-id/{id}")
    public ResponseEntity<Void> deleteUserById(@PathVariable("id") String id) {
        service.deleteUserById(id);
        return ResponseEntity.accepted().build();
    }

    /**
     * Get the profile of the currently authenticated user
     * @return the user profile
     */
    @GetMapping("/profile")
    public ResponseEntity<User> getUserProfile() {
        // Get the current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        // Assuming the username is the email in your application
        return service.getByEmail(currentUsername);
    }
}