package com.example.fileprocessor.billing;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BillingRecordRepository extends JpaRepository<BillingRecordEntity, Long> {
}
