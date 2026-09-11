package com.example.fileprocessor;

import com.example.fileprocessor.billing.BillingValidationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/upload")
public class FileProcessorController {

    private final FileProcessorService fileProcessorService;

    public FileProcessorController(FileProcessorService fileProcessorService) {
        this.fileProcessorService = fileProcessorService;
    }

    @PostMapping
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "File cannot be empty"));
        }

        try {
            String fileId = fileProcessorService.uploadFile(file.getBytes(), file.getOriginalFilename());
            var job = fileProcessorService.createProcessingJob(file.getOriginalFilename());
            fileProcessorService.processBillingFileAsync(job.getId(), file.getBytes(), file.getOriginalFilename());
            return ResponseEntity.accepted().body(Map.of(
                    "message", "File accepted for processing",
                    "fileId", fileId,
                    "jobId", job.getId(),
                    "status", "QUEUED",
                    "fileName", file.getOriginalFilename()
            ));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Error uploading file: " + e.getMessage()));
        } catch (BillingValidationException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Billing validation failed",
                    "details", e.getErrors()
            ));
        }
    }
}
