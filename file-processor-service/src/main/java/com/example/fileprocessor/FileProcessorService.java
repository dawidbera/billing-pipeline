package com.example.fileprocessor;

import com.example.fileprocessor.billing.BillingParserService;
import com.example.fileprocessor.billing.BillingPersistenceService;
import com.example.fileprocessor.billing.BillingProcessingService;
import com.example.fileprocessor.billing.BillingProcessingSummary;
import com.example.fileprocessor.billing.BillingRecord;
import com.example.fileprocessor.billing.BillingUploadSummary;
import org.springframework.beans.factory.annotation.Value;
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

    public FileProcessorService(S3Client s3Client,
                               @Value("${aws.s3.bucket}") String bucketName,
                               BillingParserService billingParserService,
                               BillingPersistenceService billingPersistenceService,
                               BillingProcessingService billingProcessingService) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
        this.billingParserService = billingParserService;
        this.billingPersistenceService = billingPersistenceService;
        this.billingProcessingService = billingProcessingService;
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
}
