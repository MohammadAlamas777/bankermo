package com.bankermo.bankermo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/hello")
    public String hello() {
        return "BankerMo is alive!";
    }

    @GetMapping("/api/me")
    public String me(org.springframework.security.core.Authentication authentication) {
        return "You are authenticated as: " + authentication.getName();
    }
}