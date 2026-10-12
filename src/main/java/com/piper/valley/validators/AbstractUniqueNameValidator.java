package com.piper.valley.validators;

import org.springframework.validation.Errors;

abstract class AbstractUniqueNameValidator<F> extends AbstractFormValidator<F> {
    @Override
    protected final void validateForm(F form, Errors errors) {
        if (errors.hasFieldErrors("name")) {
            return;
        }
        if (nameExists(nameOf(form))) {
            errors.rejectValue("name", duplicateNameCode());
        }
    }

    protected abstract String nameOf(F form);

    protected abstract boolean nameExists(String name);

    protected abstract String duplicateNameCode();
}
