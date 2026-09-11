package com.example.fileprocessor;

import com.example.fileprocessor.billing.BillingProcessingJob;
import com.example.fileprocessor.billing.BillingUploadSummary;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Controller unit tests verifying HTTP request handling, validation, and status codes for {@link FileProcessorController}.
 */
@WebMvcTest(FileProcessorController.class)
class FileProcessorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FileProcessorService fileProcessorService;

    /**
     * Verifies that a valid multipart file upload returns HTTP 200 OK along with success message and generated fileId.
     *
     * @throws Exception if mockMvc execution fails
     */
    @Test
    @DisplayName("Should successfully upload file and return 200 OK with fileId")
    void upload_validFile_returnsOk() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "sample-billing.csv",
                "text/csv",
                "id,amount\n1,100.00".getBytes(StandardCharsets.UTF_8)
        );

        BillingProcessingJob job = new BillingProcessingJob("sample-billing.csv");
        job.setId(42L);

        Mockito.when(fileProcessorService.uploadFile(any(), eq("sample-billing.csv")))
                .thenReturn("generated-uuid-12345-sample-billing.csv");
        Mockito.when(fileProcessorService.createProcessingJob("sample-billing.csv"))
                .thenReturn(job);

        mockMvc.perform(multipart("/upload").file(file))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.message").value("File accepted for processing"))
                .andExpect(jsonPath("$.fileId").value("generated-uuid-12345-sample-billing.csv"))
                .andExpect(jsonPath("$.jobId").value(42))
                .andExpect(jsonPath("$.status").value("QUEUED"));
    }

    /**
     * Verifies that uploading an empty file returns HTTP 400 Bad Request with an error description.
     *
     * @throws Exception if mockMvc execution fails
     */
    @Test
    @DisplayName("Should reject empty file with 400 Bad Request")
    void upload_emptyFile_returnsBadRequest() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.txt",
                "text/plain",
                new byte[0]
        );

        mockMvc.perform(multipart("/upload").file(emptyFile))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("File cannot be empty"));

        Mockito.verifyNoInteractions(fileProcessorService);
    }

    /**
     * Verifies that when the service layer throws an IOException, the controller returns HTTP 500 Internal Server Error.
     *
     * @throws Exception if mockMvc execution fails
     */
    @Test
    @DisplayName("Should return 500 Internal Server Error when S3 upload fails with IOException")
    void upload_serviceThrowsIOException_returnsInternalServerError() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "corrupt.dat",
                "application/octet-stream",
                "payload".getBytes(StandardCharsets.UTF_8)
        );

        Mockito.when(fileProcessorService.uploadFile(any(), any()))
                .thenThrow(new IOException("S3 connection timed out"));

        mockMvc.perform(multipart("/upload").file(file))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Error uploading file: S3 connection timed out"));
    }
}
