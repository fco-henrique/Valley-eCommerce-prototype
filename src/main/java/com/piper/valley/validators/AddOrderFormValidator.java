package com.piper.valley.validators;

import com.piper.valley.forms.AddOrderForm;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

@Component
public class AddOrderFormValidator extends AbstractFormValidator<AddOrderForm> {


	@Override
	protected Class<AddOrderForm> formType() {
		return AddOrderForm.class;
	}

	@Override
	protected void validateForm(AddOrderForm form, Errors errors) {
		validateQuantity(errors,form);
		validateAddress(errors,form);
	}

	private void validateQuantity(Errors errors,AddOrderForm form)
	{
		if(form.getQuantity() == null)
		{
			errors.rejectValue("quantity","NotEmpty");
		}
		if(form.getQuantity()<=0)
		{
			errors.rejectValue("quantity","msg.QuantityNegativeOrZero");
		}
	}

	private void validateAddress(Errors errors,AddOrderForm form)
	{
		if(form.getAddress().length() < 2 || form.getAddress().length() > 200)
			errors.rejectValue("address","msg.AddressSizeRange");
	}

}
