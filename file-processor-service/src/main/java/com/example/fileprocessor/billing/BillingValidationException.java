package com.example.fileprocessor.billing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BillingValidationException extends RuntimeException {

    private final List<String> errors;

    public BillingValidationException(List<String> errors) {
        super(String.join("; ", errors));
        this.errors = new ArrayList<>(errors);
    }

    public List<String> getErrors() {
        return Collections.unmodifiableList(errors);
    }
}
