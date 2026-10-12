package com.piper.valley.validators;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

abstract class AbstractFormValidator<T> implements Validator {
    protected abstract Class<T> formType();

    protected boolean skipWhenErrorsPresent() {
        return false;
    }

    protected abstract void validateForm(T form, Errors errors);

    @Override
    public final boolean supports(Class<?> clazz) {
        return clazz.equals(formType());
    }

    @Override
    public final void validate(Object target, Errors errors) {
        if (skipWhenErrorsPresent() && errors.hasErrors()) {
            return;
        }
        validateForm(formType().cast(target), errors);
    }
}
