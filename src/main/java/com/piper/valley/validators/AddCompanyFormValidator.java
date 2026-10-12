package com.piper.valley.validators;

import com.piper.valley.forms.AddCompanyForm;
import com.piper.valley.models.repository.CompanyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AddCompanyFormValidator extends AbstractUniqueNameValidator<AddCompanyForm> {
    @Autowired
    private CompanyRepository companyRepository;

    @Override
    protected Class<AddCompanyForm> formType() {
        return AddCompanyForm.class;
    }

    @Override
    protected String nameOf(AddCompanyForm form) {
        return form.getName();
    }

    @Override
    protected boolean nameExists(String name) {
        return companyRepository.findOneByName(name).isPresent();
    }

    @Override
    protected String duplicateNameCode() {
        return "msg.DuplicateCompanyName";
    }
}
