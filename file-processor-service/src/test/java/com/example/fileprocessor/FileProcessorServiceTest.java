package com.example.fileprocessor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import com.example.fileprocessor.billing.BillingParserService;
import com.example.fileprocessor.billing.BillingPersistenceService;
import com.example.fileprocessor.billing.BillingRecord;
import com.example.fileprocessor.billing.BillingUploadSummary;
import org.mockito.Mockito;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests verifying S3 object upload logic, key generation, and error propagation for {@link FileProcessorService}.
 */
@ExtendWith(MockitoExtension.class)
class FileProcessorServiceTest {

    @Mock
    private S3Client s3Client;

    @Captor
    private ArgumentCaptor<PutObjectRequest> putObjectRequestCaptor;

    @Captor
    private ArgumentCaptor<RequestBody> requestBodyCaptor;

    @Mock
    private BillingParserService billingParserService;

    @Mock
    private BillingPersistenceService billingPersistenceService;

    private FileProcessorService fileProcessorService;

    private static final String BUCKET_NAME = "processor-bucket";

    /**
     * Sets up the service instance with mocked dependencies before each test execution.
     */
    @BeforeEach
    void setUp() {
        fileProcessorService = new FileProcessorService(s3Client, BUCKET_NAME, billingParserService, billingPersistenceService);
    }

    /**
     * Verifies that uploading content invokes S3Client putObject with the expected bucket and key ending with filename.
     *
     * @throws IOException if I/O error occurs
     */
    @Test
    @DisplayName("Should upload file content to S3 with correct bucket and UUID prefixed key")
    void uploadFile_success() throws IOException {
        byte[] content = "Invoice 101, USD 50.00".getBytes(StandardCharsets.UTF_8);
        String fileName = "cdr-records.csv";

        String resultKey = fileProcessorService.uploadFile(content, fileName);

        assertNotNull(resultKey, "Generated fileKey should not be null");
        assertTrue(resultKey.endsWith("-" + fileName), "fileKey must end with hyphen and filename");

        verify(s3Client).putObject(putObjectRequestCaptor.capture(), requestBodyCaptor.capture());

        PutObjectRequest capturedRequest = putObjectRequestCaptor.getValue();
        assertEquals(BUCKET_NAME, capturedRequest.bucket(), "Bucket name should match configured S3 bucket");
        assertEquals(resultKey, capturedRequest.key(), "Request key should match the generated fileKey");
        assertEquals(content.length, requestBodyCaptor.getValue().contentLength(), "Content length should match uploaded payload size");
    }

    /**
     * Verifies that exceptions from S3Client are propagated when upload fails.
     */
    @Test
    @DisplayName("Should parse and persist billing records from a CSV upload")
    void processBillingFile_csv_success() {
        String csv = String.join(System.lineSeparator(),
                "customerId,invoiceNumber,amount,currency,transactionDate,description",
                "CUST-001,INV-1001,10.50,USD,2026-09-01,Monthly plan");
        BillingRecord billingRecord = new BillingRecord(
                "CUST-001",
                "INV-1001",
                new BigDecimal("10.50"),
                "USD",
                LocalDate.of(2026, 9, 1),
                "Monthly plan"
        );

        when(billingParserService.parseCsv(csv)).thenReturn(List.of(billingRecord));
        when(billingPersistenceService.save(billingRecord)).thenReturn(billingRecord);

        BillingUploadSummary summary = fileProcessorService.processBillingFile(csv.getBytes(StandardCharsets.UTF_8), "billing.csv");

        assertNotNull(summary);
        assertEquals(1, summary.getProcessedRecords());
        assertEquals("billing.csv", summary.getFileName());
        verify(billingParserService).parseCsv(csv);
        verify(billingPersistenceService).save(billingRecord);
    }

    @Test
    @DisplayName("Should propagate SdkClientException when S3Client fails")
    void uploadFile_s3Exception_propagated() {
        byte[] content = "sample payload".getBytes(StandardCharsets.UTF_8);
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenThrow(SdkClientException.create("S3 client connection reset"));

        assertThrows(SdkClientException.class, () -> fileProcessorService.uploadFile(content, "test.dat"));
    }
}
