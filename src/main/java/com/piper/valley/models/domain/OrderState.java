package com.piper.valley.models.domain;

interface OrderState {
    void process(Order order);

    void deliver(Order order);

    boolean canDeliver();
}
