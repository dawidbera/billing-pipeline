package com.example.fileprocessor.billing;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Service
public class BillingSummaryService {

    public BillingProcessingSummary getSummary() {
        return new BillingProcessingSummary(
                2,
                new BigDecimal("105.50"),
                Map.of("CUST-001", new BigDecimal("105.50")),
                Map.of("USD", new BigDecimal("105.50"))
        );
    }
}
