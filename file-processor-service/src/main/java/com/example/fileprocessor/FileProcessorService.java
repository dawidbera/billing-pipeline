package com.example.fileprocessor;

import com.example.fileprocessor.billing.BillingParserService;
import com.example.fileprocessor.billing.BillingPersistenceService;
import com.example.fileprocessor.billing.BillingRecord;
import com.example.fileprocessor.billing.BillingUploadSummary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@Service
public class FileProcessorService {

    private final S3Client s3Client;
    private final String bucketName;
    private final BillingParserService billingParserService;
    private final BillingPersistenceService billingPersistenceService;

    public FileProcessorService(S3Client s3Client,
                               @Value("${aws.s3.bucket}") String bucketName,
                               BillingParserService billingParserService,
                               BillingPersistenceService billingPersistenceService) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
        this.billingParserService = billingParserService;
        this.billingPersistenceService = billingPersistenceService;
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

        for (BillingRecord record : records) {
            billingPersistenceService.save(record);
        }

        return new BillingUploadSummary(fileName, records.size());
    }
}
