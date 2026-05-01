package com.example.taxi.user.auth;

import java.security.MessageDigest;

final class MessageDigestTiming {
    private MessageDigestTiming() {
    }

    static boolean equals(byte[] left, byte[] right) {
        return MessageDigest.isEqual(left, right);
    }
}

