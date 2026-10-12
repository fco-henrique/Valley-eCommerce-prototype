package com.piper.valley.models.factory;

import com.piper.valley.forms.AddProductForm;
import com.piper.valley.models.domain.Product;

/** GoF Factory Method: concrete creators choose and initialize the product subtype. */
public interface ProductCreator {
    Product createProduct(AddProductForm form);
}
