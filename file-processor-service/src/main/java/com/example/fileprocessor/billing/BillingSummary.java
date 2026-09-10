package com.example.fileprocessor.billing;

import java.math.BigDecimal;
import java.util.Map;

public class BillingSummary {

    private final String fileName;
    private final int processedRecords;
    private final BigDecimal totalAmount;
    private final Map<String, BigDecimal> currencyTotals;

    public BillingSummary(String fileName, int processedRecords, BigDecimal totalAmount, Map<String, BigDecimal> currencyTotals) {
        this.fileName = fileName;
        this.processedRecords = processedRecords;
        this.totalAmount = totalAmount;
        this.currencyTotals = currencyTotals;
    }

    public String getFileName() {
        return fileName;
    }

    public int getProcessedRecords() {
        return processedRecords;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public Map<String, BigDecimal> getCurrencyTotals() {
        return currencyTotals;
    }
}
