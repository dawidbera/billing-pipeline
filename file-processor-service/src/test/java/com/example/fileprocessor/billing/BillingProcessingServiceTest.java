package com.example.fileprocessor.billing;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class BillingProcessingServiceTest {

    private final BillingProcessingService service = new BillingProcessingService();

    @Test
    @DisplayName("Should aggregate totals by customer and currency")
    void shouldAggregateBillingSummary() {
        List<BillingRecord> records = List.of(
                new BillingRecord("CUST-001", "INV-1001", new BigDecimal("10.50"), "USD", LocalDate.of(2026, 9, 1), "Monthly plan"),
                new BillingRecord("CUST-001", "INV-1002", new BigDecimal("20.00"), "USD", LocalDate.of(2026, 9, 2), "Add-on"),
                new BillingRecord("CUST-002", "INV-1003", new BigDecimal("47.25"), "EUR", LocalDate.of(2026, 9, 3), "Support" )
        );

        BillingProcessingSummary summary = service.generateSummary(records);

        assertNotNull(summary);
        assertEquals(3, summary.getProcessedRecords());
        assertEquals(new BigDecimal("77.75"), summary.getTotalAmount());
        assertEquals(new BigDecimal("30.50"), summary.getCustomerTotals().get("CUST-001"));
        assertEquals(new BigDecimal("47.25"), summary.getCustomerTotals().get("CUST-002"));
        assertEquals(new BigDecimal("30.50"), summary.getCurrencyTotals().get("USD"));
        assertEquals(new BigDecimal("47.25"), summary.getCurrencyTotals().get("EUR"));
    }
}
