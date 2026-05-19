package com.paraske.EventHub.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class MediaService {
    private static final Logger logger = LoggerFactory.getLogger(MediaService.class);

    @Autowired
    private Cloudinary cloudinary;

    public String saveFile(MultipartFile file) throws IOException {
        try {
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());

            return uploadResult.get("secure_url").toString();

        } catch (IOException e) {
            logger.error("Failed to upload main image to Cloudinary", e);
            throw new IOException("Αποτυχία αποθήκευσης της εικόνας στο Cloud.", e);
        }
    }
}