package com.example.fileprocessor.billing;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

@Service
public class BillingProcessingJobService {

    private final BillingProcessingJobRepository billingProcessingJobRepository;

    public BillingProcessingJobService(BillingProcessingJobRepository billingProcessingJobRepository) {
        this.billingProcessingJobRepository = billingProcessingJobRepository;
    }

    @Transactional
    public BillingProcessingJob createJob(String fileName) {
        BillingProcessingJob job = new BillingProcessingJob(fileName);
        return billingProcessingJobRepository.save(job);
    }

    @Transactional(readOnly = true)
    public Optional<BillingProcessingJob> getJob(Long id) {
        return billingProcessingJobRepository.findById(id);
    }

    @Transactional
    public BillingProcessingJob markProcessing(Long jobId) {
        BillingProcessingJob job = billingProcessingJobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Processing job not found: " + jobId));
        job.setStatus(BillingProcessingJobStatus.PROCESSING);
        return billingProcessingJobRepository.save(job);
    }

    @Transactional
    public BillingProcessingJob markCompleted(Long jobId, BillingProcessingSummary summary) {
        BillingProcessingJob job = billingProcessingJobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Processing job not found: " + jobId));
        job.setStatus(BillingProcessingJobStatus.COMPLETED);
        job.setProcessedRecords(summary != null ? summary.getProcessedRecords() : 0);
        job.setTotalAmount(summary != null ? summary.getTotalAmount() : BigDecimal.ZERO);
        job.setCurrencyTotals(summary != null ? summary.getCurrencyTotals() : Map.of());
        return billingProcessingJobRepository.save(job);
    }

    @Transactional
    public BillingProcessingJob markFailed(Long jobId, String errorMessage) {
        BillingProcessingJob job = billingProcessingJobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Processing job not found: " + jobId));
        job.setStatus(BillingProcessingJobStatus.FAILED);
        job.setErrorMessage(errorMessage);
        return billingProcessingJobRepository.save(job);
    }
}
