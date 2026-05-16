package com.example.demo;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;

/**
 * REST controller providing endpoints to interact with LocalStack S3 bucket.
 * Supports file uploads, listings, and downloads.
 *
 * @author Antigravity
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/files")
public class FileController {

    private static final Logger log = LoggerFactory.getLogger(FileController.class);

    private final S3Client s3Client;
    private final String bucketName;

    /**
     * Constructs the controller injecting the configured S3Client and target bucket name.
     *
     * @param s3Client The Amazon S3 client bean.
     * @param bucketName The name of the S3 bucket to interact with.
     */
    public FileController(S3Client s3Client, @Value("${aws.s3.bucket-name}") String bucketName) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
    }

    /**
     * Uploads a file to the S3 bucket.
     *
     * @param file The file uploaded via multipart form data request.
     * @return A {@link ResponseEntity} indicating the result of the upload operation.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty.");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            fileName = "unnamed_" + System.currentTimeMillis();
        }

        log.info("Uploading file '{}' to bucket '{}'...", fileName, bucketName);

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            log.info("File '{}' uploaded successfully.", fileName);
            return ResponseEntity.status(HttpStatus.CREATED).body("File uploaded successfully: " + fileName);
        } catch (IOException e) {
            log.error("Failed to read input stream for file upload: {}", fileName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to read file: " + e.getMessage());
        } catch (Exception e) {
            log.error("S3 upload failed for file: {}", fileName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("S3 upload failed: " + e.getMessage());
        }
    }

    /**
     * Lists all keys (filenames) currently stored in the S3 bucket.
     *
     * @return A {@link ResponseEntity} containing a list of strings representing filenames.
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<String>> listFiles() {
        log.info("Listing files in S3 bucket '{}'...", bucketName);
        try {
            ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .build();

            ListObjectsV2Response listResponse = s3Client.listObjectsV2(listRequest);
            List<String> fileKeys = listResponse.contents().stream()
                    .map(S3Object::key)
                    .collect(Collectors.toList());

            log.info("Successfully fetched {} file keys from S3.", fileKeys.size());
            return ResponseEntity.ok(fileKeys);
        } catch (Exception e) {
            log.error("Failed to list objects in bucket '{}': ", bucketName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Downloads a file from the S3 bucket by its unique key.
     *
     * @param filename The name of the file to download from S3.
     * @return A {@link ResponseEntity} wrapping the file binary stream.
     */
    @GetMapping("/{filename}")
    public ResponseEntity<Resource> downloadFile(@PathVariable("filename") String filename) {
        log.info("Requesting download of file '{}' from S3...", filename);
        try {
            GetObjectRequest getRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(filename)
                    .build();

            ResponseInputStream<GetObjectResponse> s3Stream = s3Client.getObject(getRequest);
            GetObjectResponse responseDetails = s3Stream.response();

            InputStreamResource resource = new InputStreamResource(s3Stream);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");
            
            String contentType = responseDetails.contentType();
            MediaType mediaType = contentType != null ? MediaType.parseMediaType(contentType) : MediaType.APPLICATION_OCTET_STREAM;

            log.info("Successfully initiated download for file '{}'. Type: {}", filename, mediaType);
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(responseDetails.contentLength())
                    .contentType(mediaType)
                    .body(resource);
        } catch (Exception e) {
            log.error("Failed to retrieve file '{}' from S3: ", filename, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
