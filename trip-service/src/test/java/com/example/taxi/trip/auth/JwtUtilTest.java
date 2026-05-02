package com.example.taxi.trip.auth;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class JwtUtilTest {
    @Test
    void serviceTokenIsValid() {
        JwtUtil jwtUtil = new JwtUtil("test-secret-with-enough-length", "taxi-platform", 60);

        assertThat(jwtUtil.isValid(jwtUtil.serviceToken())).isTrue();
    }

    @Test
    void isValidRejectsMalformedToken() {
        JwtUtil jwtUtil = new JwtUtil("test-secret-with-enough-length", "taxi-platform", 60);

        assertThat(jwtUtil.isValid("not-a-token")).isFalse();
    }
}

