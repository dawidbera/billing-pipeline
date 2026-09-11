package com.example.fileprocessor.billing;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BillingProcessingJobRepository extends JpaRepository<BillingProcessingJob, Long> {
}
