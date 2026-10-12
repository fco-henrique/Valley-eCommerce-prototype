package com.piper.valley.models.domain;

import com.piper.valley.models.enums.OrderStatus;
import java.util.Date;

final class UnprocessedOrderState implements OrderState {
    @Override
    public void process(Order order) {
        order.setOrderStatus(OrderStatus.PROCESSED);
        order.setProcessedDate(new Date());
    }

    @Override
    public void deliver(Order order) {
        throw new IllegalStateException("Unprocessed order cannot be delivered");
    }

    @Override
    public boolean canDeliver() {
        return false;
    }
}
