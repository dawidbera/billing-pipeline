package com.example.fileprocessor.billing;

import java.math.BigDecimal;
import java.time.LocalDate;

public class BillingRecord {

    private String customerId;
    private String invoiceNumber;
    private BigDecimal amount;
    private String currency;
    private LocalDate transactionDate;
    private String description;

    public BillingRecord() {
    }

    public BillingRecord(String customerId, String invoiceNumber, BigDecimal amount,
                        String currency, LocalDate transactionDate, String description) {
        this.customerId = customerId;
        this.invoiceNumber = invoiceNumber;
        this.amount = amount;
        this.currency = currency;
        this.transactionDate = transactionDate;
        this.description = description;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
