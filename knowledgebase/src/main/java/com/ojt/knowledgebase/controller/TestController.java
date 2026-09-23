package com.ojt.knowledgebase.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/api/test")
    public String test() {
        return "Project Knowledge Base API is running!";
    }

    @GetMapping("/api/protected/test")
    public String protectedTest() {
        return "JWT authentication successful!";
    }
}