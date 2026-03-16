package com.recognitionservice.recognitionservice.services;

import com.recognitionservice.recognitionservice.enums.ContentFlag;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class RecognitionService {
    public ContentFlag flagFile(byte[] fileData) {
        ContentFlag contentFlag;

        Random rand = new Random();
        int randomNumber = rand.nextInt(1, 3);
        switch (randomNumber) {
            case 1 -> contentFlag = ContentFlag.SAFE;
            case 2 -> contentFlag = ContentFlag.RISK;
            default -> throw new RuntimeException("Unexpected contentFlag: " + randomNumber);
        }
        return contentFlag;
    }
}
