package com.pcd.user.controllers;


import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    @PostMapping("users")
    public String addUser(
           // @RequestParam("name") String name, @RequestParam("password") String password
    ) {
        return "hello" ; //name + password;
    }
    @GetMapping("users")
    public String adddUser(
            // @RequestParam("name") String name, @RequestParam("password") String password
    ) {
        return "get" ; //name + password;
    }
}
