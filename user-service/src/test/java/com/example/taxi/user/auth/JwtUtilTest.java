package com.example.taxi.user.auth;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class JwtUtilTest {
    @Test
    void createTokenProducesValidToken() {
        JwtUtil jwtUtil = new JwtUtil("test-secret-with-enough-length", "taxi-platform", 60);

        String token = jwtUtil.createToken("student", "ADMIN");

        assertThat(jwtUtil.isValid(token)).isTrue();
    }

    @Test
    void isValidRejectsTamperedToken() {
        JwtUtil jwtUtil = new JwtUtil("test-secret-with-enough-length", "taxi-platform", 60);
        String token = jwtUtil.createToken("student", "ADMIN");

        assertThat(jwtUtil.isValid(token + "x")).isFalse();
    }
}

