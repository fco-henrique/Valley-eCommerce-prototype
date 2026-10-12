package com.piper.valley.validators;

import com.piper.valley.auth.CurrentUser;
import com.piper.valley.forms.PromoteAdminForm;
import com.piper.valley.models.domain.User;
import com.piper.valley.models.enums.Role;
import com.piper.valley.utilities.AuthUtil;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

@Component
public class PromoteAdminFormValidator extends AbstractAdminFormValidator<PromoteAdminForm> {
    @Override
    protected Class<PromoteAdminForm> formType() {
        return PromoteAdminForm.class;
    }

    @Override
    protected String usernameOf(PromoteAdminForm form) {
        return form.getUsername();
    }

    @Override
    protected String missingUserCode() {
        return "NotValid";
    }

    @Override
    protected void validateAdmin(User user, Errors errors) {
        if (user.getRoles().contains(Role.ADMIN)) {
            errors.rejectValue("username", "msg.AlreadyAdmin");
        }

        CurrentUser currentUser = AuthUtil.getCurrentUser();
        if (user.getId().equals(currentUser.getUser().getId())) {
            errors.rejectValue("username", "msg.PromoteSelf");
        }
    }
}
