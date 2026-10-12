package com.piper.valley.models.domain;

import com.piper.valley.models.enums.OrderStatus;
import java.util.Date;

final class ProcessedOrderState implements OrderState {
    @Override
    public void process(Order order) {
        throw new IllegalStateException("Order has already been processed");
    }

    @Override
    public void deliver(Order order) {
        order.setOrderStatus(OrderStatus.DELIVERED);
        order.setDeliveredDate(new Date());
    }

    @Override
    public boolean canDeliver() {
        return true;
    }
}
