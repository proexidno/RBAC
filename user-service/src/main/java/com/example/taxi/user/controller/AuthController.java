package com.example.taxi.user.controller;

import com.example.taxi.user.auth.JwtUtil;
import com.example.taxi.user.dto.AuthRequest;
import com.example.taxi.user.dto.AuthResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final JwtUtil jwtUtil;

    public AuthController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/token")
    AuthResponse token(@Valid @RequestBody AuthRequest request) {
        return new AuthResponse(jwtUtil.createToken(request.subject(), request.role()));
    }
}

