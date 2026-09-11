package com.example.fileprocessor;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;

/**
 * End-to-end integration test verifying file upload, persistence, and retrieval against LocalStack S3 container.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class FileProcessorIntegrationTest {

    private static final String TEST_BUCKET = "processor-bucket";

    private static LocalStackContainer localStack;

    @Autowired
    private FileProcessorService fileProcessorService;

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        if (isDockerAvailable()) {
            localStack = new LocalStackContainer(DockerImageName.parse("localstack/localstack:3.3.0")).withServices(S3);
            localStack.start();
            registry.add("aws.s3.endpoint", () -> localStack.getEndpointOverride(S3).toString());
            registry.add("aws.s3.region", localStack::getRegion);
            registry.add("aws.s3.access-key", localStack::getAccessKey);
            registry.add("aws.s3.secret-key", localStack::getSecretKey);
            registry.add("aws.s3.bucket", () -> TEST_BUCKET);
            return;
        }

        registry.add("aws.s3.endpoint", () -> "http://localhost:4566");
        registry.add("aws.s3.region", () -> "us-east-1");
        registry.add("aws.s3.access-key", () -> "test");
        registry.add("aws.s3.secret-key", () -> "test");
        registry.add("aws.s3.bucket", () -> TEST_BUCKET);
    }

    private static boolean isDockerAvailable() {
        try {
            DockerClientFactory.instance().client();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static S3Client createLocalStackClient(String endpoint, String region, String accessKey, String secretKey) {
        S3ClientBuilder builder = S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)
                ));
        return builder.build();
    }

    /**
     * Verifies that a file uploaded via FileProcessorService is successfully written to S3
     * and can be read back with exact binary and text content matching.
     *
     * @throws IOException if upload or retrieval fails
     */
    @Test
    @DisplayName("Should successfully upload and read back file from LocalStack S3")
    void uploadAndReadBack_success() throws IOException {
        Assumptions.assumeTrue(isDockerAvailable(),
                "Docker/Testcontainers is not available in this environment; skipping LocalStack integration test");

        String endpoint = localStack.getEndpointOverride(S3).toString();
        String region = localStack.getRegion();
        String accessKey = localStack.getAccessKey();
        String secretKey = localStack.getSecretKey();

        try (S3Client s3Client = createLocalStackClient(endpoint, region, accessKey, secretKey)) {
            try {
                s3Client.createBucket(CreateBucketRequest.builder().bucket(TEST_BUCKET).build());
            } catch (Exception ignored) {
                // bucket may already exist in an existing LocalStack instance
            }

            String testContent = "accountId,callDurationSec,rate\nACC-001,120,0.05\nACC-002,45,0.05";
            String fileName = "cdr-integration-test.csv";

            String fileKey = fileProcessorService.uploadFile(testContent.getBytes(StandardCharsets.UTF_8), fileName);

            assertNotNull(fileKey, "Returned S3 key must not be null");
            assertTrue(fileKey.endsWith("-" + fileName), "Returned key must retain original filename suffix");

            ResponseBytes<GetObjectResponse> responseBytes = s3Client.getObjectAsBytes(
                    GetObjectRequest.builder()
                            .bucket(TEST_BUCKET)
                            .key(fileKey)
                            .build()
            );

            assertEquals(testContent, responseBytes.asUtf8String(), "S3 stored content must match original payload");
        }
    }
}
