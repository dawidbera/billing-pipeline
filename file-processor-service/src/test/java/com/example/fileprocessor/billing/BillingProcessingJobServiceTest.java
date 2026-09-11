package com.example.fileprocessor.billing;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BillingProcessingJobServiceTest {

    @Mock
    private BillingProcessingJobRepository billingProcessingJobRepository;

    @InjectMocks
    private BillingProcessingJobService billingProcessingJobService;

    @Test
    @DisplayName("Should create a queued job and mark it complete with a summary")
    void shouldCreateQueuedJobAndCompleteIt() {
        BillingProcessingJob createdJob = new BillingProcessingJob();
        createdJob.setId(1L);
        createdJob.setFileName("billing.csv");
        createdJob.setStatus(BillingProcessingJobStatus.QUEUED);

        BillingProcessingJob persistedJob = new BillingProcessingJob();
        persistedJob.setId(1L);
        persistedJob.setFileName("billing.csv");
        persistedJob.setStatus(BillingProcessingJobStatus.QUEUED);

        BillingProcessingJob completedJob = new BillingProcessingJob();
        completedJob.setId(1L);
        completedJob.setFileName("billing.csv");
        completedJob.setStatus(BillingProcessingJobStatus.COMPLETED);
        completedJob.setProcessedRecords(2);
        completedJob.setTotalAmount(new BigDecimal("105.50"));

        when(billingProcessingJobRepository.findById(1L)).thenReturn(Optional.of(persistedJob));
        when(billingProcessingJobRepository.save(any(BillingProcessingJob.class)))
                .thenReturn(createdJob)
                .thenReturn(completedJob);

        BillingProcessingJob created = billingProcessingJobService.createJob("billing.csv");
        BillingProcessingJob updated = billingProcessingJobService.markCompleted(1L, new BillingProcessingSummary(
                2,
                new BigDecimal("105.50"),
                Map.of("CUST-001", new BigDecimal("105.50")),
                Map.of("USD", new BigDecimal("105.50"))
        ));

        assertEquals(BillingProcessingJobStatus.QUEUED, created.getStatus());
        assertEquals(BillingProcessingJobStatus.COMPLETED, updated.getStatus());
        assertEquals(2, updated.getProcessedRecords());
        assertEquals(new BigDecimal("105.50"), updated.getTotalAmount());
    }
}
