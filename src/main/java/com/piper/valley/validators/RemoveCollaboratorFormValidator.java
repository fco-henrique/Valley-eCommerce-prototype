package com.piper.valley.validators;

import com.piper.valley.auth.CurrentUser;
import com.piper.valley.models.domain.Store;
import com.piper.valley.models.domain.User;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

@Component
public class RemoveCollaboratorFormValidator extends AbstractCollaboratorValidator {
    @Override
    protected String missingUserCode() {
        return "msg.NotValide";
    }

    @Override
    protected String missingStoreCode() {
        return "msg.NotValide";
    }

    @Override
    protected String unauthorizedCode() {
        return "Unauthorized!!!!";
    }

    @Override
    protected void validateCollaborator(User user, Store store, CurrentUser currentUser, Errors errors) {
        if (user.getId() == currentUser.getUser().getId()) {
            errors.rejectValue("username", "msg.DuplicateStoreOwner");
            return;
        }
        if (user.getStoreOwner() != null && !store.getCollaborators().contains(user.getStoreOwner())) {
            errors.rejectValue("username", "msg.NotCollaborator");
        }
    }
}
