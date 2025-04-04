package com.pcd.user.controllers;


import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    @PostMapping("users")
    public String addUser(@RequestParam("name") String name, @RequestParam("password") String password) {
        return name + password;
    }
}
