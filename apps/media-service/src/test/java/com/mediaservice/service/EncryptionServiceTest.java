package com.mediaservice.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@ExtendWith(MockitoExtension.class)
public class EncryptionServiceTest {
    @InjectMocks
    private EncryptionService encryptionService;

    @Test
    @DisplayName("Hashing should not result in differences")
    public void createHash_String_byteArray() {
        // Variables
        String mockData = "Hobbithon123!";

        // Act
        byte[] result_1 = encryptionService.createHash(mockData);
        byte[] result_2 = encryptionService.createHash(mockData);

        // Assert
        assertArrayEquals(result_1, result_2);
    }

    @Test
    @DisplayName("Hashing should result in differences")
    public void createHash_difference_String_byteArray() {
        // Variables
        String mockData_1 = "Hobbithon123!";
        String mockData_2 = "HobbithOn123!";

        // Act
        byte[] result_1 = encryptionService.createHash(mockData_1);
        byte[] result_2 = encryptionService.createHash(mockData_2);

        // Assert
        assertFalse(Arrays.equals(result_1, result_2));
    }
}
