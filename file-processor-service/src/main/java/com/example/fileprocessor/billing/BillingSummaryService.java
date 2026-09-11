package com.example.fileprocessor.billing;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BillingSummaryService {

    private final BillingRecordRepository billingRecordRepository;

    public BillingSummaryService(BillingRecordRepository billingRecordRepository) {
        this.billingRecordRepository = billingRecordRepository;
    }

    public BillingProcessingSummary getSummary() {
        List<BillingRecordEntity> records = billingRecordRepository.findAll();

        if (records == null || records.isEmpty()) {
            return new BillingProcessingSummary(0, BigDecimal.ZERO, Map.of(), Map.of());
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        Map<String, BigDecimal> customerTotals = new HashMap<>();
        Map<String, BigDecimal> currencyTotals = new HashMap<>();

        for (BillingRecordEntity record : records) {
            if (record == null || record.getAmount() == null) {
                continue;
            }

            totalAmount = totalAmount.add(record.getAmount());
            customerTotals.merge(record.getCustomerId(), record.getAmount(), BigDecimal::add);
            currencyTotals.merge(record.getCurrency(), record.getAmount(), BigDecimal::add);
        }

        return new BillingProcessingSummary(records.size(), totalAmount, customerTotals, currencyTotals);
    }
}
