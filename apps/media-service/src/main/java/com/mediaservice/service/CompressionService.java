package com.mediaservice.service;

import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

@Service
public class CompressionService {
    /**
     * Decoding the file contents from a DataURI
     *
     * @param dataUri, String
     * @return fileContents, in the form of byte[]
     */
    public byte[] decodeDataUri(String dataUri) {
        String base64 = dataUri.split(",")[1];
        return Base64.getDecoder().decode(base64);
    }

    /**
     * Extracts the contentType from a DataURI
     * If contentType contains "@file/{mime}", it is changed to an image
     *
     * @param dataUri, String
     * @return contentType, in the form of String
     */
    public String extractContentType(String dataUri) {
        String meta = dataUri.substring(5, dataUri.indexOf(","));
        String type = meta.split(";")[0];

        if (type.startsWith("@file/")) {
            return "image/" + type.substring(6);
        }

        return type;
    }

    /**
     * Used to set size and quality for uniform regular compression
     *
     * @param input, byte[] original file
     * @return byte[], compressed file
     */
    public byte[] resizeToUniform(byte[] input) {
        int size = 1600;
        float quality = 0.8f;

        return this.resize(input, size, quality);
    }

    /**
     * Used to set size and quality for uniform thumbnail compression
     *
     * @param input, byte[] original uniform file
     * @return byte[], compressed thumbnail file
     */
    public byte[] resizeToThumb(byte[] input) {
        int size = 300;
        float quality = 0.7f;

        return this.resize(input, size, quality);
    }

    /**
     * Logic for file compression
     *
     * @param input, byte[] of the file
     * @param size, int that decides how large the file should be in pixels
     * @param quality, float that decides quality percentage
     * @return byte[], compressed file
     */
    private byte[] resize(byte[] input, int size, float quality) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            Thumbnails.of(new ByteArrayInputStream(input))
                    .size(size, size)
                    .outputFormat("png")
                    .outputQuality(quality)
                    .toOutputStream(output);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return output.toByteArray();
    }
}
