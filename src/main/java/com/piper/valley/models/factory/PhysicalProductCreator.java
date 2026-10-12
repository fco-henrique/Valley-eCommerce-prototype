package com.piper.valley.models.factory;

import com.piper.valley.forms.AddProductForm;
import com.piper.valley.models.domain.PhysicalProduct;
import org.springframework.stereotype.Component;

@Component("physicalProductCreator")
public class PhysicalProductCreator implements ProductCreator {
    @Override
    public PhysicalProduct createProduct(AddProductForm form) {
        PhysicalProduct product = new PhysicalProduct();
        product.setLength(form.getLength());
        product.setWidth(form.getWidth());
        product.setHeight(form.getHeight());
        product.setWeight(form.getWeight());
        return product;
    }
}
