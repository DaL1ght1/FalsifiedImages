package com.pcd.user.controllers;


import com.pcd.user.User;
import com.pcd.user.UserRequest;
import com.pcd.user.UserResponse;
import com.pcd.user.UserService;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
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

    @DeleteMapping("/delete-user-by-id/{id}")
    public ResponseEntity<Void> deleteUserById(@PathVariable("id") String id) {
        service.deleteUserById(id);
        return ResponseEntity.accepted().build();
    }
}
