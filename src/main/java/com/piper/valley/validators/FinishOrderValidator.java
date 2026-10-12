package com.piper.valley.validators;

import com.piper.valley.forms.FinishOrderForm;
import com.piper.valley.models.domain.Order;
import com.piper.valley.models.domain.Store;
import com.piper.valley.models.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class FinishOrderValidator extends AbstractExistingStoreResourceValidator<FinishOrderForm, Order> {
    @Autowired
    private OrderService orderService;

    @Override
    protected Class<FinishOrderForm> formType() {
        return FinishOrderForm.class;
    }

    @Override
    protected Optional<Order> findResource(FinishOrderForm form) {
        return orderService.getOrderById(form.getOrderId());
    }

    @Override
    protected String fieldName() {
        return "orderId";
    }

    @Override
    protected String missingMessage() {
        return "Order doesn't exist!";
    }

    @Override
    protected boolean isValidState(Order order) {
        return order.canBeDelivered();
    }

    @Override
    protected String invalidStateMessage() {
        return "Un-valid Order";
    }

    @Override
    protected Store storeOf(Order order) {
        return order.getStoreProduct().getStore();
    }
}
