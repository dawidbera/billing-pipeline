package com.example.fileprocessor.billing;

public class BillingUploadSummary {

    private final String fileName;
    private final int processedRecords;

    public BillingUploadSummary(String fileName, int processedRecords) {
        this.fileName = fileName;
        this.processedRecords = processedRecords;
    }

    public String getFileName() {
        return fileName;
    }

    public int getProcessedRecords() {
        return processedRecords;
    }
}
