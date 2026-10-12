package com.piper.valley.validators;

import com.piper.valley.models.domain.Store;
import com.piper.valley.models.service.AuthService;
import com.piper.valley.utilities.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;

import java.util.Optional;

abstract class AbstractExistingStoreResourceValidator<F, R> extends AbstractFormValidator<F> {
    @Autowired
    private AuthService authService;

    @Override
    protected final boolean skipWhenErrorsPresent() {
        return true;
    }

    @Override
    protected final void validateForm(F form, Errors errors) {
        Optional<R> resource = findResource(form);
        if (!resource.isPresent()) {
            errors.rejectValue(fieldName(), "NotValid", missingMessage());
            return;
        }

        R value = resource.get();
        if (!isValidState(value)) {
            errors.rejectValue(fieldName(), "NotValid", invalidStateMessage());
            return;
        }

        if (!authService.canAccessStore(storeOf(value), AuthUtil.getCurrentUser())) {
            errors.rejectValue(fieldName(), "msg.NotAuthorized", "You're not Authorized to do that!");
        }
    }

    protected abstract Optional<R> findResource(F form);

    protected abstract String fieldName();

    protected abstract String missingMessage();

    protected abstract boolean isValidState(R resource);

    protected abstract String invalidStateMessage();

    protected abstract Store storeOf(R resource);
}
