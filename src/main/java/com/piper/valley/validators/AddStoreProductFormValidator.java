package com.piper.valley.validators;

import com.piper.valley.models.service.AuthService;
import com.piper.valley.auth.CurrentUser;
import com.piper.valley.forms.AddStoreProductForm;
import com.piper.valley.models.domain.*;
import com.piper.valley.models.enums.StoreStatus;
import com.piper.valley.models.service.ProductService;
import com.piper.valley.models.service.StoreService;
import com.piper.valley.utilities.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import java.util.Optional;

@Component
public class AddStoreProductFormValidator extends AbstractFormValidator<AddStoreProductForm> {
	@Autowired
	private ProductService productService;

	@Autowired
	private StoreService storeService;

	@Autowired
	private AuthService authService;

	@Override
	protected Class<AddStoreProductForm> formType() {
		return AddStoreProductForm.class;
	}

	@Override
	protected boolean skipWhenErrorsPresent() {
		return true;
	}

	@Override
	protected void validateForm(AddStoreProductForm form, Errors errors) {

		Optional<Product> productOptional = productService.getProductById(form.getProductId());
		if(!productOptional.isPresent()) {
			errors.rejectValue("productId", "NotValid");
			return;
		}

		Optional<Store> storeOptional = storeService.getStoreById(form.getStoreId());
		if(!storeOptional.isPresent()) {
			errors.rejectValue("storeId", "NotValid");
			return;
		}

		Product product = productOptional.get();
		Store store = storeOptional.get();

		CurrentUser currentUser = AuthUtil.getCurrentUser();
		if(!authService.canAccessStore(store, currentUser))
			errors.rejectValue("storeId","msg.NotAuthorized");

		if(store.getStatus() != StoreStatus.ACCEPTED)
			errors.rejectValue("storeId","msg.AcceptedStore");

		//Check Type
		if(store instanceof VirtualStore && product instanceof PhysicalProduct)
			errors.rejectValue("productId", "msg.VirtualStoreProducts");

	}
}
