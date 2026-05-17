package com.rukshan.ranaswanu;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
public class TestController {

    @GetMapping("/user")
    public User test() {
        return new User(1, "Amal", "example@gmail.com");
    }
}