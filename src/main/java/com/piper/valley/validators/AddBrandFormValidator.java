package com.piper.valley.validators;

import com.piper.valley.forms.AddBrandForm;
import com.piper.valley.models.repository.BrandRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AddBrandFormValidator extends AbstractUniqueNameValidator<AddBrandForm> {
    @Autowired
    private BrandRepository brandRepository;

    @Override
    protected Class<AddBrandForm> formType() {
        return AddBrandForm.class;
    }

    @Override
    protected String nameOf(AddBrandForm form) {
        return form.getName();
    }

    @Override
    protected boolean nameExists(String name) {
        return brandRepository.findOneByName(name).isPresent();
    }

    @Override
    protected String duplicateNameCode() {
        return "msg.DuplicateBrandName";
    }
}
