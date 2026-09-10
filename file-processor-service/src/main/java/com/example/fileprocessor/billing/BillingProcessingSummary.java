package com.example.fileprocessor.billing;

import java.math.BigDecimal;
import java.util.Map;

public class BillingProcessingSummary {

    private final int processedRecords;
    private final BigDecimal totalAmount;
    private final Map<String, BigDecimal> customerTotals;
    private final Map<String, BigDecimal> currencyTotals;

    public BillingProcessingSummary(int processedRecords,
                                   BigDecimal totalAmount,
                                   Map<String, BigDecimal> customerTotals,
                                   Map<String, BigDecimal> currencyTotals) {
        this.processedRecords = processedRecords;
        this.totalAmount = totalAmount;
        this.customerTotals = customerTotals;
        this.currencyTotals = currencyTotals;
    }

    public int getProcessedRecords() {
        return processedRecords;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public Map<String, BigDecimal> getCustomerTotals() {
        return customerTotals;
    }

    public Map<String, BigDecimal> getCurrencyTotals() {
        return currencyTotals;
    }
}
