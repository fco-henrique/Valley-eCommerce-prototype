package com.piper.valley.validators;

import com.piper.valley.auth.CurrentUser;
import com.piper.valley.models.domain.Store;
import com.piper.valley.models.domain.User;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

@Component
public class AddCollaboratorFormValidator extends AbstractCollaboratorValidator {
    @Override
    protected String missingUserCode() {
        return "msg.UsernameNotFound";
    }

    @Override
    protected String missingStoreCode() {
        return "msg.NotValid";
    }

    @Override
    protected String unauthorizedCode() {
        return "msg.NotAuthorized";
    }

    @Override
    protected void validateCollaborator(User user, Store store, CurrentUser currentUser, Errors errors) {
        if (store.getCollaborators().contains(user.getStoreOwner())) {
            errors.rejectValue("username", "msg.DuplicateCollaborator");
            return;
        }
        if (user.getId() == currentUser.getUser().getId()) {
            errors.rejectValue("username", "msg.DuplicateStoreOwner");
        }
    }
}
