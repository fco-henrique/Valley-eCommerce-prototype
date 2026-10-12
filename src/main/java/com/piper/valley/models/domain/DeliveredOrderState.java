package com.piper.valley.models.domain;

final class DeliveredOrderState implements OrderState {
    @Override
    public void process(Order order) {
        throw new IllegalStateException("Delivered order cannot be processed again");
    }

    @Override
    public void deliver(Order order) {
        throw new IllegalStateException("Order has already been delivered");
    }

    @Override
    public boolean canDeliver() {
        return false;
    }
}
