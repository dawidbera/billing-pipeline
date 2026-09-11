package com.example.fileprocessor.billing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BillingSummaryServiceTest {

    @Mock
    private BillingRecordRepository billingRecordRepository;

    @InjectMocks
    private BillingSummaryService billingSummaryService;

    @BeforeEach
    void setUp() {
        BillingRecordEntity record1 = new BillingRecordEntity(
                "CUST-001",
                "INV-1001",
                new BigDecimal("10.50"),
                "USD",
                LocalDate.of(2026, 9, 1),
                "Monthly plan"
        );
        BillingRecordEntity record2 = new BillingRecordEntity(
                "CUST-002",
                "INV-1002",
                new BigDecimal("95.00"),
                "USD",
                LocalDate.of(2026, 9, 2),
                "Support plan"
        );

        when(billingRecordRepository.findAll()).thenReturn(List.of(record1, record2));
    }

    @Test
    @DisplayName("Should calculate persisted billing summary from database records")
    void shouldCalculateSummaryFromDatabaseRecords() {
        BillingProcessingSummary summary = billingSummaryService.getSummary();

        assertEquals(2, summary.getProcessedRecords());
        assertEquals(new BigDecimal("105.50"), summary.getTotalAmount());
        assertEquals(new BigDecimal("105.50"), summary.getCurrencyTotals().get("USD"));
        assertEquals(new BigDecimal("10.50"), summary.getCustomerTotals().get("CUST-001"));
        assertEquals(new BigDecimal("95.00"), summary.getCustomerTotals().get("CUST-002"));
    }
}
