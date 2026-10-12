package com.piper.valley.validators;

import com.piper.valley.models.domain.User;
import com.piper.valley.models.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;

import java.util.Optional;

abstract class AbstractAdminFormValidator<F> extends AbstractFormValidator<F> {
    @Autowired
    private UserService userService;

    @Override
    protected final boolean skipWhenErrorsPresent() {
        return true;
    }

    @Override
    protected final void validateForm(F form, Errors errors) {
        Optional<User> user = userService.getUserByUsername(usernameOf(form));
        if (!user.isPresent()) {
            errors.rejectValue("username", missingUserCode());
            return;
        }
        validateAdmin(user.get(), errors);
    }

    protected abstract String usernameOf(F form);

    protected abstract String missingUserCode();

    protected abstract void validateAdmin(User user, Errors errors);
}
