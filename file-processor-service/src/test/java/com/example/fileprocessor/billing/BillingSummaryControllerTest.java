package com.example.fileprocessor.billing;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BillingSummaryController.class)
class BillingSummaryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BillingSummaryService billingSummaryService;

    @Test
    @DisplayName("Should return aggregated billing summary as JSON")
    void shouldReturnBillingSummary() throws Exception {
        BillingProcessingSummary summary = new BillingProcessingSummary(
                2,
                new BigDecimal("105.50"),
                Map.of("CUST-001", new BigDecimal("105.50")),
                Map.of("USD", new BigDecimal("105.50"))
        );

        when(billingSummaryService.getSummary()).thenReturn(summary);

        mockMvc.perform(get("/billing/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.processedRecords").value(2))
                .andExpect(jsonPath("$.totalAmount").value(105.50))
                .andExpect(jsonPath("$.currencyTotals.USD").value(105.50));
    }
}
