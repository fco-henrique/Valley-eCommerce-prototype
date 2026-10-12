package com.piper.valley.validators;

import com.piper.valley.auth.CurrentUser;
import com.piper.valley.forms.DemoteAdminForm;
import com.piper.valley.models.domain.Admin;
import com.piper.valley.models.domain.User;
import com.piper.valley.models.enums.Role;
import com.piper.valley.utilities.AuthUtil;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import java.util.Objects;

@Component
public class DemoteAdminFormValidator extends AbstractAdminFormValidator<DemoteAdminForm> {
    @Override
    protected Class<DemoteAdminForm> formType() {
        return DemoteAdminForm.class;
    }

    @Override
    protected String usernameOf(DemoteAdminForm form) {
        return form.getUsername();
    }

    @Override
    protected String missingUserCode() {
        return "msg.UsernameNotFound";
    }

    @Override
    protected void validateAdmin(User user, Errors errors) {
        if (!user.getRoles().contains(Role.ADMIN)) {
            errors.rejectValue("username", "msg.NotAdmin");
            return;
        }

        CurrentUser currentUser = AuthUtil.getCurrentUser();
        if (user.getId().equals(currentUser.getUser().getId())) {
            errors.rejectValue("username", "msg.PromoteSelf");
        }

        if (user.getAdmin().getSuperior().getId() != currentUser.getId()) {
            Admin admin = user.getAdmin().getSuperior();
            while (true) {
                if (admin.getId() == currentUser.getId()) {
                    break;
                } else if (Objects.equals(admin.getId(), admin.getSuperior().getId())) {
                    errors.rejectValue("username", "msg.NotYourSub");
                    break;
                }
                admin = admin.getSuperior();
            }
        }
    }
}
