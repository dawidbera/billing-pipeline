package com.example.fileprocessor;

import com.example.fileprocessor.billing.BillingParserService;
import com.example.fileprocessor.billing.BillingPersistenceService;
import com.example.fileprocessor.billing.BillingProcessingJob;
import com.example.fileprocessor.billing.BillingProcessingJobService;
import com.example.fileprocessor.billing.BillingProcessingService;
import com.example.fileprocessor.billing.BillingProcessingSummary;
import com.example.fileprocessor.billing.BillingRecord;
import com.example.fileprocessor.billing.BillingUploadSummary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class FileProcessorService {

    private final S3Client s3Client;
    private final String bucketName;
    private final BillingParserService billingParserService;
    private final BillingPersistenceService billingPersistenceService;
    private final BillingProcessingService billingProcessingService;
    private final BillingProcessingJobService billingProcessingJobService;

    public FileProcessorService(S3Client s3Client,
                               @Value("${aws.s3.bucket}") String bucketName,
                               BillingParserService billingParserService,
                               BillingPersistenceService billingPersistenceService,
                               BillingProcessingService billingProcessingService,
                               BillingProcessingJobService billingProcessingJobService) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
        this.billingParserService = billingParserService;
        this.billingPersistenceService = billingPersistenceService;
        this.billingProcessingService = billingProcessingService;
        this.billingProcessingJobService = billingProcessingJobService;
    }

    public String uploadFile(byte[] content, String fileName) throws IOException {
        String fileKey = UUID.randomUUID() + "-" + fileName;

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileKey)
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(content));

        return fileKey;
    }

    public BillingUploadSummary processBillingFile(byte[] content, String fileName) {
        String payload = new String(content, StandardCharsets.UTF_8);
        List<BillingRecord> records;

        if (fileName.toLowerCase().endsWith(".json")) {
            records = billingParserService.parseJson(payload);
        } else {
            records = billingParserService.parseCsv(payload);
        }

        Map<String, BigDecimal> currencyTotals = new HashMap<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (BillingRecord record : records) {
            billingPersistenceService.save(record);

            BigDecimal amount = record.getAmount();
            totalAmount = totalAmount.add(amount);
            currencyTotals.merge(record.getCurrency(), amount, BigDecimal::add);
        }

        BillingProcessingSummary processingSummary = billingProcessingService.generateSummary(records);
        return new BillingUploadSummary(
                fileName,
                processingSummary.getProcessedRecords(),
                processingSummary.getTotalAmount(),
                processingSummary.getCurrencyTotals()
        );
    }

    @Async
    public void processBillingFileAsync(Long jobId, byte[] content, String fileName) {
        try {
            billingProcessingJobService.markProcessing(jobId);
            BillingUploadSummary summary = processBillingFile(content, fileName);
            billingProcessingJobService.markCompleted(jobId, new BillingProcessingSummary(
                    summary.getProcessedRecords(),
                    summary.getTotalAmount(),
                    Map.of(),
                    summary.getCurrencyTotals()
            ));
        } catch (Exception e) {
            billingProcessingJobService.markFailed(jobId, e.getMessage());
        }
    }

    public BillingProcessingJob createProcessingJob(String fileName) {
        return billingProcessingJobService.createJob(fileName);
    }
}
