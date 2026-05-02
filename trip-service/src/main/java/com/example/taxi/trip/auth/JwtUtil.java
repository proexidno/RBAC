package com.example.taxi.trip.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {
    private final String secret;
    private final String issuer;
    private final long ttlSeconds;

    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.issuer}") String issuer,
                   @Value("${jwt.ttl-seconds}") long ttlSeconds) {
        this.secret = secret;
        this.issuer = issuer;
        this.ttlSeconds = ttlSeconds;
    }

    public String createToken(String subject, String role) {
        long exp = Instant.now().plusSeconds(ttlSeconds).getEpochSecond();
        String header = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
        String payload = "{\"sub\":\"" + escape(subject) + "\",\"role\":\"" + escape(role)
                + "\",\"iss\":\"" + escape(issuer) + "\",\"exp\":" + exp + "}";
        String body = encode(header.getBytes(StandardCharsets.UTF_8)) + "."
                + encode(payload.getBytes(StandardCharsets.UTF_8));
        return body + "." + sign(body);
    }

    public String serviceToken() {
        return createToken("trip-service", "SERVICE");
    }

    public boolean isValid(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return false;
            }
            String body = parts[0] + "." + parts[1];
            if (!MessageDigest.isEqual(sign(body).getBytes(StandardCharsets.UTF_8), parts[2].getBytes(StandardCharsets.UTF_8))) {
                return false;
            }
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            Long exp = extractLong(payload, "\"exp\":");
            return exp != null && exp > Instant.now().getEpochSecond() && payload.contains("\"iss\":\"" + issuer + "\"");
        } catch (RuntimeException ex) {
            return false;
        }
    }

    private String sign(String body) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return encode(mac.doFinal(body.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot sign JWT", ex);
        }
    }

    private static String encode(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static Long extractLong(String json, String marker) {
        int start = json.indexOf(marker);
        if (start < 0) {
            return null;
        }
        start += marker.length();
        int end = start;
        while (end < json.length() && Character.isDigit(json.charAt(end))) {
            end++;
        }
        return Long.parseLong(json.substring(start, end));
    }
}
