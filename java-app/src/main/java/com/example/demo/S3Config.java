package com.example.demo;

import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * Spring configuration class for initializing the Amazon S3 client bean.
 * Configures the client to connect to either a local LocalStack instance or a standard AWS endpoint
 * based on application properties.
 *
 * @author Antigravity
 * @version 1.0.0
 */
@Configuration
public class S3Config {

    @Value("${aws.s3.endpoint}")
    private String endpoint;

    @Value("${aws.s3.region}")
    private String region;

    @Value("${aws.s3.access-key-id}")
    private String accessKeyId;

    @Value("${aws.s3.secret-access-key}")
    private String secretAccessKey;

    /**
     * Instantiates and registers the S3Client bean.
     * Overrides the default service endpoint URL to target the LocalStack instance, and configures static dummy credentials.
     *
     * @return A configured {@link S3Client} instance.
     */
    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKeyId, secretAccessKey)
                ))
                // Force path-style addressing for LocalStack compatibility (avoids resolving bucket-name.localhost)
                .forcePathStyle(true)
                .build();
    }
}
