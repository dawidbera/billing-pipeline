package com.example.fileprocessor.billing;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BillingParserService {

    private final ObjectMapper objectMapper;

    public BillingParserService() {
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /**
     * Parses a CSV payload containing billing rows and validates each record.
     * The initial implementation expects comma-delimited values without embedded quoted commas.
     */
    public List<BillingRecord> parseCsv(String content) {
        if (content == null || content.isBlank()) {
            throw new BillingValidationException(List.of("Billing file content is empty"));
        }

        String[] lines = content.split("\\R");
        if (lines.length < 2) {
            throw new BillingValidationException(List.of("Billing CSV must contain header and at least one data row"));
        }

        List<BillingRecord> records = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (int i = 1; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) {
                continue;
            }

            String[] values = line.split(",", -1);
            if (values.length != 6) {
                errors.add("Row " + (i + 1) + " must contain exactly 6 fields");
                continue;
            }

            try {
                BillingRecord record = new BillingRecord();
                record.setCustomerId(requireNonBlank(values[0], "customerId", i + 1));
                record.setInvoiceNumber(requireNonBlank(values[1], "invoiceNumber", i + 1));
                record.setAmount(parseAmount(values[2], i + 1));
                record.setCurrency(requireNonBlank(values[3], "currency", i + 1));
                record.setTransactionDate(parseDate(values[4], i + 1));
                record.setDescription(values[5] == null ? "" : values[5].trim());
                records.add(record);
            } catch (BillingValidationException validationException) {
                errors.addAll(validationException.getErrors());
            }
        }

        if (!errors.isEmpty()) {
            throw new BillingValidationException(errors);
        }

        return Collections.unmodifiableList(records);
    }

    /**
     * Parses a JSON array of billing records and validates each row.
     */
    public List<BillingRecord> parseJson(String content) {
        if (content == null || content.isBlank()) {
            throw new BillingValidationException(List.of("Billing JSON content is empty"));
        }

        try {
            List<BillingRecord> records = objectMapper.readValue(content, new TypeReference<>() {});
            List<String> errors = new ArrayList<>();

            for (int i = 0; i < records.size(); i++) {
                BillingRecord record = records.get(i);
                validateRecord(record, i + 1, errors);
            }

            if (!errors.isEmpty()) {
                throw new BillingValidationException(errors);
            }

            return Collections.unmodifiableList(records);
        } catch (IOException e) {
            throw new BillingValidationException(List.of("Unable to parse billing JSON: " + e.getMessage()));
        }
    }

    private void validateRecord(BillingRecord record, int rowNumber, List<String> errors) {
        if (record == null) {
            errors.add("Row " + rowNumber + " is null");
            return;
        }

        if (record.getCustomerId() == null || record.getCustomerId().isBlank()) {
            errors.add("Row " + rowNumber + " has missing customerId");
        }
        if (record.getInvoiceNumber() == null || record.getInvoiceNumber().isBlank()) {
            errors.add("Row " + rowNumber + " has missing invoiceNumber");
        }
        if (record.getAmount() == null) {
            errors.add("Row " + rowNumber + " has missing amount");
        } else if (record.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            errors.add("Row " + rowNumber + " has invalid amount: must be positive");
        }
        if (record.getCurrency() == null || record.getCurrency().isBlank()) {
            errors.add("Row " + rowNumber + " has missing currency");
        }
        if (record.getTransactionDate() == null) {
            errors.add("Row " + rowNumber + " has missing transactionDate");
        }
    }

    private String requireNonBlank(String value, String fieldName, int rowNumber) {
        if (value == null || value.isBlank()) {
            throw new BillingValidationException(List.of("Row " + rowNumber + " has missing " + fieldName));
        }
        return value.trim();
    }

    private BigDecimal parseAmount(String rawAmount, int rowNumber) {
        String value = requireNonBlank(rawAmount, "amount", rowNumber);
        try {
            BigDecimal amount = new BigDecimal(value);
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BillingValidationException(List.of("Row " + rowNumber + " has invalid amount: must be positive"));
            }
            return amount;
        } catch (NumberFormatException e) {
            throw new BillingValidationException(List.of("Row " + rowNumber + " has invalid amount: '" + rawAmount + "'"));
        }
    }

    private LocalDate parseDate(String rawDate, int rowNumber) {
        String value = requireNonBlank(rawDate, "transactionDate", rowNumber);
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            throw new BillingValidationException(List.of("Row " + rowNumber + " has invalid transactionDate: '" + rawDate + "'"));
        }
    }
}
