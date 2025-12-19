package com.example.justdoit.utils.validation.rules;

public interface IValidationRule {
    boolean isValid(String value);
    String getErrorMessage();
}
