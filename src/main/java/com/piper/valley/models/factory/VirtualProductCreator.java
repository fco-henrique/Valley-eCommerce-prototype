package com.piper.valley.models.factory;

import com.piper.valley.forms.AddProductForm;
import com.piper.valley.models.domain.VirtualProduct;
import org.springframework.stereotype.Component;

@Component("virtualProductCreator")
public class VirtualProductCreator implements ProductCreator {
    @Override
    public VirtualProduct createProduct(AddProductForm form) {
        VirtualProduct product = new VirtualProduct();
        product.setSerial(form.getSerial());
        return product;
    }
}
