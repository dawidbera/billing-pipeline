package com.example.fileprocessor.billing;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/billing")
public class BillingProcessingJobController {

    private final BillingProcessingJobService billingProcessingJobService;

    public BillingProcessingJobController(BillingProcessingJobService billingProcessingJobService) {
        this.billingProcessingJobService = billingProcessingJobService;
    }

    @GetMapping("/jobs/{id}")
    public ResponseEntity<?> getJobStatus(@PathVariable Long id) {
        BillingProcessingJob job = billingProcessingJobService.getJob(id)
                .orElseThrow(() -> new IllegalArgumentException("Processing job not found: " + id));

        return ResponseEntity.ok(Map.of(
                "jobId", job.getId(),
                "fileName", job.getFileName(),
                "status", job.getStatus().name(),
                "processedRecords", job.getProcessedRecords() == null ? 0 : job.getProcessedRecords(),
                "totalAmount", job.getTotalAmount() == null ? "0.00" : job.getTotalAmount().toPlainString(),
                "currencyTotals", job.getCurrencyTotals() == null ? Map.of() : job.getCurrencyTotals(),
                "errorMessage", job.getErrorMessage() == null ? "" : job.getErrorMessage()
        ));
    }
}
