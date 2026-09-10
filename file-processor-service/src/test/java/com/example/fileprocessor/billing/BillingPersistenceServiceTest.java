package com.example.fileprocessor.billing;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@Import(BillingPersistenceService.class)
@ActiveProfiles("test")
class BillingPersistenceServiceTest {

    @Autowired
    private BillingRecordRepository billingRecordRepository;

    @Autowired
    private BillingPersistenceService billingPersistenceService;

    @Test
    @DisplayName("Should save a valid billing record to the database")
    void shouldPersistValidBillingRecord() {
        BillingRecord billingRecord = new BillingRecord(
                "CUST-200",
                "INV-2001",
                new BigDecimal("245.80"),
                "USD",
                LocalDate.of(2026, 9, 10),
                "Enterprise plan"
        );

        BillingRecord saved = billingPersistenceService.save(billingRecord);

        assertNotNull(saved);
        assertEquals(1, billingRecordRepository.count());
        assertEquals("CUST-200", billingRecordRepository.findAll().get(0).getCustomerId());
    }
}
