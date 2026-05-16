package com.example.fileprocessor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/upload")
public class FileProcessorController {

    @Autowired
    private FileProcessorService fileProcessorService;

    @PostMapping
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) {
        try {
            String fileId = fileProcessorService.uploadFile(file.getBytes(), file.getOriginalFilename());
            return ResponseEntity.ok(Map.of(
                    "message", "File uploaded successfully",
                    "fileId", fileId
            ));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Error uploading file: " + e.getMessage());
        }
    }
}
