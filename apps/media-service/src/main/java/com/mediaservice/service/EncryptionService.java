package com.mediaservice.service;

import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Service
public class EncryptionService {
    /**
     * Creates the hash for a given string
     * SHA-256
     *
     * @param data base64 string of file data
     * @return byte[] as hash
     */
    public byte[] createHash(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(
                    data.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
