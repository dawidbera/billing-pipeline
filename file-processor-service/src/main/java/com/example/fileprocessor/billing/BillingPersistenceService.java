package com.example.fileprocessor.billing;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BillingPersistenceService {

    private final BillingRecordRepository billingRecordRepository;

    public BillingPersistenceService(BillingRecordRepository billingRecordRepository) {
        this.billingRecordRepository = billingRecordRepository;
    }

    @Transactional
    public BillingRecord save(BillingRecord billingRecord) {
        if (billingRecord == null) {
            throw new IllegalArgumentException("Billing record must not be null");
        }

        BillingRecordEntity entity = new BillingRecordEntity(
                billingRecord.getCustomerId(),
                billingRecord.getInvoiceNumber(),
                billingRecord.getAmount(),
                billingRecord.getCurrency(),
                billingRecord.getTransactionDate(),
                billingRecord.getDescription()
        );

        BillingRecordEntity saved = billingRecordRepository.save(entity);
        return new BillingRecord(
                saved.getCustomerId(),
                saved.getInvoiceNumberValue(),
                saved.getAmount(),
                saved.getCurrency(),
                saved.getTransactionDate(),
                saved.getDescription()
        );
    }
}
