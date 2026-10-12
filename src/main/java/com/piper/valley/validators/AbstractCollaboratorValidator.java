package com.piper.valley.validators;

import com.piper.valley.auth.CurrentUser;
import com.piper.valley.forms.AddStoreCollaboratorForm;
import com.piper.valley.models.domain.Store;
import com.piper.valley.models.domain.User;
import com.piper.valley.models.service.AuthService;
import com.piper.valley.models.service.StoreService;
import com.piper.valley.models.service.UserService;
import com.piper.valley.utilities.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;

import java.util.Optional;

abstract class AbstractCollaboratorValidator extends AbstractFormValidator<AddStoreCollaboratorForm> {
    @Autowired
    private StoreService storeService;

    @Autowired
    private UserService userService;

    @Autowired
    private AuthService authService;

    @Override
    protected final Class<AddStoreCollaboratorForm> formType() {
        return AddStoreCollaboratorForm.class;
    }

    @Override
    protected final boolean skipWhenErrorsPresent() {
        return true;
    }

    @Override
    protected final void validateForm(AddStoreCollaboratorForm form, Errors errors) {
        Optional<User> user = userService.getUserByUsername(form.getUsername());
        if (!user.isPresent()) {
            errors.rejectValue("username", missingUserCode());
            return;
        }

        Optional<Store> store = storeService.getStoreById(form.getStoreId());
        if (!store.isPresent()) {
            errors.rejectValue("storeId", missingStoreCode());
            return;
        }

        CurrentUser currentUser = AuthUtil.getCurrentUser();
        if (!authService.canAccessStore(store.get(), currentUser)) {
            errors.rejectValue("storeId", unauthorizedCode());
            return;
        }

        validateCollaborator(user.get(), store.get(), currentUser, errors);
    }

    protected abstract String missingUserCode();

    protected abstract String missingStoreCode();

    protected abstract String unauthorizedCode();

    protected abstract void validateCollaborator(User user, Store store, CurrentUser currentUser, Errors errors);
}
