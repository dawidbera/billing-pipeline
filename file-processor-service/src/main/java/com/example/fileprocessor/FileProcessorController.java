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
            var summary = fileProcessorService.processBillingFile(file.getBytes(), file.getOriginalFilename());
            return ResponseEntity.ok(Map.of(
                    "message", "File uploaded successfully",
                    "fileId", fileId,
                    "processedRecords", summary.getProcessedRecords(),
                    "fileName", summary.getFileName(),
                    "totalAmount", summary.getTotalAmount(),
                    "currencyTotals", summary.getCurrencyTotals()
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
