package com.paraske.EventHub.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.cloudinary.utils.ObjectUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MediaServiceTest {

    @InjectMocks
    private MediaService mediaService;

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private Uploader uploader;

    @Mock
    private MultipartFile multipartFile;

    @Test
    void saveFile_shouldReturnUrl_whenUploadIsSuccessful() throws IOException {
        String expectedUrl = "http://res.cloudinary.com/demo/image/upload/v1571218039/sample.jpg";
        Map<String, String> uploadResult = Map.of("secure_url", expectedUrl);

        when(multipartFile.getBytes()).thenReturn(new byte[0]);
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), any(Map.class))).thenReturn(uploadResult);

        String actualUrl = mediaService.saveFile(multipartFile);

        assertEquals(expectedUrl, actualUrl);
    }

    @Test
    void saveFile_shouldThrowIOException_whenUploadFails() throws IOException {
        when(multipartFile.getBytes()).thenReturn(new byte[0]);
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), any(Map.class))).thenThrow(new IOException("Upload failed"));

        assertThrows(IOException.class, () -> mediaService.saveFile(multipartFile));
    }
}