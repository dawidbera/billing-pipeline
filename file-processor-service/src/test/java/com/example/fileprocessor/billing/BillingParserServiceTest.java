package com.example.fileprocessor.billing;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BillingParserServiceTest {

    private final BillingParserService billingParserService = new BillingParserService();

    @Test
    @DisplayName("Should parse valid CSV billing records")
    void parseCsv_shouldAcceptValidRecords() {
        String csv = String.join(System.lineSeparator(),
                "customerId,invoiceNumber,amount,currency,transactionDate,description",
                "CUST-001,INV-1001,125.50,USD,2026-09-01,Monthly subscription",
                "CUST-002,INV-1002,49.99,EUR,2026-09-02,Support plan"
        );

        List<BillingRecord> records = billingParserService.parseCsv(csv);

        assertEquals(2, records.size());
        assertEquals("CUST-001", records.get(0).getCustomerId());
        assertEquals("INV-1001", records.get(0).getInvoiceNumber());
        assertEquals("USD", records.get(0).getCurrency());
        assertEquals("Monthly subscription", records.get(0).getDescription());
    }

    @Test
    @DisplayName("Should reject invalid billing CSV records")
    void parseCsv_shouldRejectInvalidRecords() {
        String csv = String.join(System.lineSeparator(),
                "customerId,invoiceNumber,amount,currency,transactionDate,description",
                "CUST-001,INV-1003,-10.00,USD,2026-09-03,Invalid negative amount"
        );

        BillingValidationException exception = assertThrows(BillingValidationException.class,
                () -> billingParserService.parseCsv(csv));

        assertFalse(exception.getErrors().isEmpty());
        assertTrue(exception.getErrors().stream().anyMatch(error -> error.contains("amount")));
    }

    @Test
    @DisplayName("Should parse JSON billing records")
    void parseJson_shouldAcceptValidPayload() {
        String json = """
                [
                  {
                    "customerId": "CUST-010",
                    "invoiceNumber": "INV-777",
                    "amount": 89.10,
                    "currency": "PLN",
                    "transactionDate": "2026-09-04",
                    "description": "Development service"
                  }
                ]
                """;

        List<BillingRecord> records = billingParserService.parseJson(json);

        assertEquals(1, records.size());
        assertEquals("CUST-010", records.get(0).getCustomerId());
        assertEquals("INV-777", records.get(0).getInvoiceNumber());
    }
}
