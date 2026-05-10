package com.example.taxi.notification.auth;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;

class JwtUtilTest {
    @Test
    void isValidAcceptsSignedToken() {
        JwtUtil jwtUtil = new JwtUtil("test-secret-with-enough-length", "taxi-platform");

        assertThat(jwtUtil.isValid(token("test-secret-with-enough-length", "taxi-platform",
                Instant.now().plusSeconds(60).getEpochSecond()))).isTrue();
    }

    @Test
    void isValidRejectsExpiredToken() {
        JwtUtil jwtUtil = new JwtUtil("test-secret-with-enough-length", "taxi-platform");

        assertThat(jwtUtil.isValid(token("test-secret-with-enough-length", "taxi-platform",
                Instant.now().minusSeconds(60).getEpochSecond()))).isFalse();
    }

    private String token(String secret, String issuer, long exp) {
        String header = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
        String payload = "{\"sub\":\"trip-service\",\"role\":\"SERVICE\",\"iss\":\"" + issuer + "\",\"exp\":" + exp + "}";
        String body = encode(header.getBytes(StandardCharsets.UTF_8)) + "."
                + encode(payload.getBytes(StandardCharsets.UTF_8));
        return body + "." + sign(secret, body);
    }

    private String sign(String secret, String body) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return encode(mac.doFinal(body.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    private String encode(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}

