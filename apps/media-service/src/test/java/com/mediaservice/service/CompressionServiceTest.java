package com.mediaservice.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class CompressionServiceTest {
    @InjectMocks
    private CompressionService compressionService;

    @Test
    @DisplayName("Should decode file data from dataURI")
    public void decodeDataUri_String_ByteArray() {
        // Variables
        String mockURI = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAFcAAABXCAMAAABGFileAAAAIVBMVEXzshrzshrzshrzshrzshr0uS/////98tr64KHzshr3zGhiIae7AAAABnRSTlMAHFLalv5XsvhaAAACYElEQVR42r2ZW24sIQxEg99m/wu+Uks3ViAGB5KuT0ZzpqZwNw9/VNQAiAgRiQigffyGgNBHIcEVswF6Jjz2DeRrHbkG9L0Qjqi/TW7odVE5Z/Kfie7M5sJ2mOx9yuBngoNo70MmPxcdYO/A5Hei2ykz6Qn4CuuScB2uHocuwuUHBL0sE/NEeDFnHDFsI25eVxf1XO00BVex8M5Lw+B1scSssUpf1QTWHbqJemCFF1MHW4dic/E+2O6ToJyuBTiKN8M6DnZrYFZbY8MweRkcyrFOUbt1cI4NtXqRqQR4iY0gsPaq+QLOsTFztWchwPMrgq3PQUAp3RHMAe0qInMQVE4hwKH+/KQOfqkUL6vM4ICK9e8CrqYwg1Ueq+yzonq3KcxgETXOKhgKpZuAeVXBUE8hZPutBO1TSMB3XJUEfMc1SWRXXJZUtubiJoUzMK79dlnJTnNgWcsOuSob2RG3y1aWcuE8BcvBsOCarKTdPcAztx3WAv//6WxFzlLQjdlHmuxZ8/XCtmYfcbog049T6L4TpeuxXnEhtlHFFFQr4Jbsdzi3ygHe7Heg7FefWdqDIdlP9u/BPf6M7mOYK0IlwENxlfzSvF/3+J5NZuNjq92d4ICdwcZ1LMZ5aMBOYPMSNuzOhrvIDFauY3E4b2ZgfQZ4jw27IUzB5s4PuIil4TyfgPlzgDmwu9oNUQaOgRIW8vuSAOswsMfi4n4nDI4DdnIBCj6AdRiovXe34MDG0WojevG+7x5ML96n3oPphfvqF+7XL/sBr/Uv7i1je6E/9EI/673+W2jXL3y/v/lX/dh/IxjAtjkThhoAAAAASUVORK5CYII=";
        String fileData = "iVBORw0KGgoAAAANSUhEUgAAAFcAAABXCAMAAABGFileAAAAIVBMVEXzshrzshrzshrzshrzshr0uS/////98tr64KHzshr3zGhiIae7AAAABnRSTlMAHFLalv5XsvhaAAACYElEQVR42r2ZW24sIQxEg99m/wu+Uks3ViAGB5KuT0ZzpqZwNw9/VNQAiAgRiQigffyGgNBHIcEVswF6Jjz2DeRrHbkG9L0Qjqi/TW7odVE5Z/Kfie7M5sJ2mOx9yuBngoNo70MmPxcdYO/A5Hei2ykz6Qn4CuuScB2uHocuwuUHBL0sE/NEeDFnHDFsI25eVxf1XO00BVex8M5Lw+B1scSssUpf1QTWHbqJemCFF1MHW4dic/E+2O6ToJyuBTiKN8M6DnZrYFZbY8MweRkcyrFOUbt1cI4NtXqRqQR4iY0gsPaq+QLOsTFztWchwPMrgq3PQUAp3RHMAe0qInMQVE4hwKH+/KQOfqkUL6vM4ICK9e8CrqYwg1Ueq+yzonq3KcxgETXOKhgKpZuAeVXBUE8hZPutBO1TSMB3XJUEfMc1SWRXXJZUtubiJoUzMK79dlnJTnNgWcsOuSob2RG3y1aWcuE8BcvBsOCarKTdPcAztx3WAv//6WxFzlLQjdlHmuxZ8/XCtmYfcbog049T6L4TpeuxXnEhtlHFFFQr4Jbsdzi3ygHe7Heg7FefWdqDIdlP9u/BPf6M7mOYK0IlwENxlfzSvF/3+J5NZuNjq92d4ICdwcZ1LMZ5aMBOYPMSNuzOhrvIDFauY3E4b2ZgfQZ4jw27IUzB5s4PuIil4TyfgPlzgDmwu9oNUQaOgRIW8vuSAOswsMfi4n4nDI4DdnIBCj6AdRiovXe34MDG0WojevG+7x5ML96n3oPphfvqF+7XL/sBr/Uv7i1je6E/9EI/673+W2jXL3y/v/lX/dh/IxjAtjkThhoAAAAASUVORK5CYII=";
        byte[] expectedResult = Base64.getDecoder().decode(fileData);
        // Act
        byte[] result = compressionService.decodeDataUri(mockURI);

        // Assert
        assertArrayEquals(result, expectedResult);
    }

    @Test
    @DisplayName("Should extract content type from dataURI")
    public void extractContentType_String_String() {
        // Variables
        String mockURI = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAFcAAABXCAMAAABGFileAAAAIVBMVEXzshrzshrzshrzshrzshr0uS/////98tr64KHzshr3zGhiIae7AAAABnRSTlMAHFLalv5XsvhaAAACYElEQVR42r2ZW24sIQxEg99m/wu+Uks3ViAGB5KuT0ZzpqZwNw9/VNQAiAgRiQigffyGgNBHIcEVswF6Jjz2DeRrHbkG9L0Qjqi/TW7odVE5Z/Kfie7M5sJ2mOx9yuBngoNo70MmPxcdYO/A5Hei2ykz6Qn4CuuScB2uHocuwuUHBL0sE/NEeDFnHDFsI25eVxf1XO00BVex8M5Lw+B1scSssUpf1QTWHbqJemCFF1MHW4dic/E+2O6ToJyuBTiKN8M6DnZrYFZbY8MweRkcyrFOUbt1cI4NtXqRqQR4iY0gsPaq+QLOsTFztWchwPMrgq3PQUAp3RHMAe0qInMQVE4hwKH+/KQOfqkUL6vM4ICK9e8CrqYwg1Ueq+yzonq3KcxgETXOKhgKpZuAeVXBUE8hZPutBO1TSMB3XJUEfMc1SWRXXJZUtubiJoUzMK79dlnJTnNgWcsOuSob2RG3y1aWcuE8BcvBsOCarKTdPcAztx3WAv//6WxFzlLQjdlHmuxZ8/XCtmYfcbog049T6L4TpeuxXnEhtlHFFFQr4Jbsdzi3ygHe7Heg7FefWdqDIdlP9u/BPf6M7mOYK0IlwENxlfzSvF/3+J5NZuNjq92d4ICdwcZ1LMZ5aMBOYPMSNuzOhrvIDFauY3E4b2ZgfQZ4jw27IUzB5s4PuIil4TyfgPlzgDmwu9oNUQaOgRIW8vuSAOswsMfi4n4nDI4DdnIBCj6AdRiovXe34MDG0WojevG+7x5ML96n3oPphfvqF+7XL/sBr/Uv7i1je6E/9EI/673+W2jXL3y/v/lX/dh/IxjAtjkThhoAAAAASUVORK5CYII=";
        String expectedResult = "image/png";

        // Act
        String result = compressionService.extractContentType(mockURI);

        // Assert
        assertEquals(result, expectedResult);
    }

    @Test
    @DisplayName("resizeToThumb should return a smaller image")
    public void testResizeToThumb_byteArray_byteArray() throws IOException {
        // Arrange: load a small test image from resources
        InputStream is = getClass().getResourceAsStream("/test-image.png");
        assertNotNull(is, "Test image should exist in resources");
        byte[] inputBytes = is.readAllBytes();

        // Act
        byte[] thumbBytes = compressionService.resizeToThumb(inputBytes);

        // Assert
        assertNotNull(thumbBytes, "Output should not be null");
        assertTrue(thumbBytes.length < inputBytes.length, "Thumbnail should be smaller than input. " + thumbBytes.length + "<" + inputBytes.length);

        BufferedImage thumbImage = ImageIO.read(new ByteArrayInputStream(thumbBytes));
        assertNotNull(thumbImage, "Thumbnail should be a readable image");
        assertEquals(200, thumbImage.getWidth());
        assertEquals(200, thumbImage.getHeight());
    }

    @Test
    @DisplayName("resizeToUniform should return a uniform image")
    public void testResizeToUniform_byteArray_byteArray() throws IOException {
        // Arrange: load a small test image from resources
        InputStream is = getClass().getResourceAsStream("/test-image.png");
        assertNotNull(is, "Test image should exist in resources");
        byte[] inputBytes = is.readAllBytes();

        // Act
        byte[] thumbBytes = compressionService.resizeToUniform(inputBytes);

        // Assert
        assertNotNull(thumbBytes, "Output should not be null");

        BufferedImage thumbImage = ImageIO.read(new ByteArrayInputStream(thumbBytes));
        assertNotNull(thumbImage, "Thumbnail should be a readable image");
        assertEquals(1600, thumbImage.getWidth());
        assertEquals(1600, thumbImage.getHeight());
    }
}
