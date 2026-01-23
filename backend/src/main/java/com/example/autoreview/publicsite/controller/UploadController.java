package com.example.autoreview.publicsite.controller;

import java.util.Map;

import com.example.autoreview.publicsite.service.UploadService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/uploads")
public class UploadController {

    private final UploadService uploadService;
    private final String fileBaseUrl;

    public UploadController(UploadService uploadService, @Value("${app.file-base-url:}") String fileBaseUrl) {
        this.uploadService = uploadService;
        this.fileBaseUrl = fileBaseUrl;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        String path = uploadService.store(file);
        String url = buildUrl(path);
        return ResponseEntity.ok(Map.of("path", path, "url", url));
    }

    private String buildUrl(String path) {
        if (fileBaseUrl == null || fileBaseUrl.isBlank()) {
            return path;
        }
        String base = fileBaseUrl.endsWith("/") ? fileBaseUrl.substring(0, fileBaseUrl.length() - 1) : fileBaseUrl;
        String normalizedPath = path.startsWith("/") ? path : "/" + path;
        return base + normalizedPath;
    }
}
