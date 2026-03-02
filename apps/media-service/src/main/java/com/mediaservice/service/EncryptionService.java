package com.mediaservice.service;

import org.springframework.stereotype.Service;

import javax.crypto.Mac;

@Service
public class EncryptionService {
    /**
     * Creates the hash for a given string
     *
     * @param data base64 string of file data
     * @return byte[] as hash
     */
    public byte[] createHash(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            return mac.doFinal(data.getBytes());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
