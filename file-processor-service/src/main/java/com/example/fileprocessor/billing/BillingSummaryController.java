package com.example.fileprocessor.billing;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/billing")
public class BillingSummaryController {

    private final BillingSummaryService billingSummaryService;

    public BillingSummaryController(BillingSummaryService billingSummaryService) {
        this.billingSummaryService = billingSummaryService;
    }

    @GetMapping("/summary")
    public ResponseEntity<BillingProcessingSummary> getSummary() {
        return ResponseEntity.ok(billingSummaryService.getSummary());
    }
}
